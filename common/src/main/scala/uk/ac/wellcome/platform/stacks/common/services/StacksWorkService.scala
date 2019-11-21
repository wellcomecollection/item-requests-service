package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import com.google.gson.internal.LinkedTreeMap
import org.threeten.bp.{ZoneId, ZoneOffset}
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.catalogue.models.{ItemIdentifiers, ResultList, ResultListItems}
import uk.ac.wellcome.platform.sierra.models.{Hold, HoldResultSet, PatronHoldPost}
import uk.ac.wellcome.platform.stacks.common.models.{StacksHold, StacksHoldRequest, StacksHoldStatus, StacksItem, StacksItemStatus, StacksLocation, StacksPickup, StacksUserHolds, StacksWork}

import scala.collection.JavaConverters._
import scala.collection.Set
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


case class StacksUserIdentifier(value: String)
case class StacksWorkIdentifier(value: String)

sealed trait ItemIdentifier {
  val value: String
}
case class SierraItemIdentifier(value: String) extends ItemIdentifier

object SierraItemIdentifier {
  def apply(hold: Hold): SierraItemIdentifier = {
    SierraItemIdentifier(
      hold.getRecord.split("/").last
    )
  }
}

case class CatalogueItemIdentifier(value: String) extends ItemIdentifier

case class StacksItemIdentifier(
                                 catalogueId: CatalogueItemIdentifier,
                                 sierraId: SierraItemIdentifier
                               )

import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._


class StacksWorkService(
                         val worksApi: WorksApi,
                         val sierraService: SierraService
                       )(
                       implicit val ec: ExecutionContext
) {
  type LTM[T] = LinkedTreeMap[String, T]

  implicit class LinkedTreeMapExtractor(ltm: LTM[Any]) {
    def safeGet[T](key: String): Option[T] =
      Option(ltm.get(key)).flatMap(o => Try(o.asInstanceOf[T]).toOption)
  }

  protected def getItemIdentifiersFrom(item: ResultListItems): List[ItemIdentifiers] =
    item.getIdentifiers.asScala.toList

  protected def getItemsFrom(workId: StacksWorkIdentifier): Future[List[ResultListItems]] =
    Future {
      worksApi.getWork(workId.value, "items,identifiers")
    }.map(_.getItems.asScala.toList)

  protected def getStacksItemIdentifierFrom(item: ResultListItems): Future[StacksItemIdentifier] = {
    val identifier = getItemIdentifiersFrom(item)
      .filter { _.getIdentifierType.getId == "sierra-identifier" }
      .map { _.getValue }

    identifier match {
      case List(sierraItemId) => {
        val catalogueId = CatalogueItemIdentifier(item.getId)
        val sierraId = SierraItemIdentifier(sierraItemId)

        Future.successful(StacksItemIdentifier(catalogueId, sierraId))
      }
      case _ => Future.failed(new Throwable(
        f"Ambiguous or missing identifier! ($identifier)"
      ))
    }
  }

  protected def getStacksLocationFrom(item: ResultListItems): Future[Option[StacksLocation]] = {
    val itemLocations = item.getLocations.asScala.toList

    val physicalLocation = itemLocations
      .map(_.asInstanceOf[LTM[Any]])
      .map { l =>
        (
          l.safeGet[LTM[String]]("locationType").map(_.get("id")),
          l.safeGet[String]("type"),
          l.safeGet[String]("label")
        )
      }
      .collect {
        case (Some(id), Some("PhysicalLocation"), Some(label)) =>
          StacksLocation(id, label)
      }

    physicalLocation match {
      case List(location) => Future.successful(Some(location))
      case Nil => Future.successful(None)
      case _ => Future.failed(new Throwable(
        f"Ambiguous location! ($itemLocations)"
      ))
    }
  }

  protected def getItemStatus(sierraId: SierraItemIdentifier): Future[StacksItemStatus] = for {
    itemsApi <- sierraService.itemsApi()
    sierraItem = itemsApi.getAnItemByRecordID(
      sierraId.value, List.empty[String].asJava
    )
  } yield StacksItemStatus(sierraItem.getStatus.getCode)

  def getStacksWork(workId: StacksWorkIdentifier): Future[StacksWork] = {
    val eventuallyStacksItems = for {
      items <- getItemsFrom(workId)
      stacksItems <- items.traverse(getStacksItemIdentifierFrom)
    } yield stacksItems

    for {
      idsAndLocations <- eventuallyIdsAndLocations
      stacksItems <- idsAndLocations.traverse {
        case (itemId, Some(location)) => getItemStatus(itemId.sierraId).map { status =>
          StacksItem(itemId.catalogueId.value, location, status)
        }
      }
    } yield StacksWork(workId.value, stacksItems)
  }

  // ----

  protected def getStacksItemIdentifierFrom(identifier: ItemIdentifier): Future[StacksItem] = {
    val eventuallyItems = Future {
      // The generated client forces this nasty interface
      val worksResultList = worksApi.getWorks(
        "items,identifiers",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        f"i${identifier.value}",
        null,
        null
      ).getResults.asScala.toList

      worksResultList match {
        case headWork :: _ => headWork.getItems.asScala.toList
        case _ => throw new Exception("No matching works found!")
      }
    }

    for {
      items <- eventuallyItems
      itemIdentifiers <- items.traverse(getStacksItemIdentifierFrom)
      stacksLocations <- items.traverse(getStacksLocationFrom)

      itemIdentifier = itemIdentifiers.toSet.toList match {
        case List(one) => one
        case _ => throw new Exception(
          f"Ambiguous or missing item record!"
        )
      }

      stacksLocation = stacksLocations.toSet.toList match {
        case List(Some(one)) => one
        case _ => throw new Exception(
          f"Ambiguous or missing location for item record!"
        )
      }

      _ = identifier match {
        case id@SierraItemIdentifier(_) =>
          if(itemIdentifier.sierraId != id)
            throw new Exception(f"Sierra item record ID mismatch!")
        case id@CatalogueItemIdentifier(_) =>
          if(itemIdentifier.catalogueId != id)
            throw new Exception(f"Catalogue item record ID mismatch!")
      }
    } yield StacksItem(
      id = itemIdentifier,
      location = stacksLocation
    )
  }

  protected def getHoldResultSet(userIdentity: StacksUserIdentifier): Future[HoldResultSet] = for {
    patronsApi <- sierraService.patronsApi()
    holdResultSet = patronsApi.getTheHoldsDataForASinglePatronRecord(
      userIdentity.value, 100, 0,
      List.empty[String].asJava
    )
  } yield holdResultSet

  protected def getStacksPickupFromHold(hold: Hold): StacksPickup = {
    StacksPickup(
      location =  StacksLocation(
        hold.getPickupLocation.getCode,
        hold.getPickupLocation.getName
      ),
      // This should be a simpler conversion to a Java Instant
      pickUpBy = Instant.ofEpochSecond(
        hold.getPickupByDate.toEpochSecond
      )
    )
  }

  protected def getStacksHoldStatusFrom(hold: Hold): StacksHoldStatus = {
    StacksHoldStatus(
      id = hold.getStatus.getCode,
      label = hold.getStatus.getName
    )
  }

  protected def getStacksItemIdentifierFrom(hold: Hold): Future[StacksItemIdentifier] = {
    hold.getRecordType match {
      case "i" => getStacksItemIdentifierFrom(SierraItemIdentifier(hold))
      case _ => Future.failed(
        new Throwable(f"Could not get item record from hold! ($hold)")
      )
    }
  }

  def getStacksUserHolds(userId: StacksUserIdentifier): Future[StacksUserHolds] = for {
    holdResultSet <- getHoldResultSet(userId)
    entries = holdResultSet.getEntries.asScala.toList

    stacksItemsIdentifiers <- entries.traverse(getStacksItemIdentifierFrom)
    holdStatuses = entries.map(getStacksHoldStatusFrom)
    stacksPickups = entries.map(getStacksPickupFromHold)

    userHolds = (stacksItemsIdentifiers, holdStatuses, stacksPickups)
      .zipped.toList map {
      case (stacksItemsIdentifier, holdStatus, stacksPickup) =>
            StacksHold(
              itemId = stacksItemsIdentifier.catalogueId.value,
              pickup = stacksPickup,
              status = holdStatus
            )
    }

  } yield StacksUserHolds(
    userId = userId.value,
    holds = userHolds,
  )

  // ----

  protected def placeHoldInSierra(
                                   userIdentifier: StacksUserIdentifier,
                                   sierraItemIdentifier: SierraItemIdentifier
                                 ): Future[Unit] = for {
    itemLocation <- getItemLocation(sierraItemIdentifier)
    patronsApi <- sierraService.patronsApi()
    patronHoldPost = new PatronHoldPost()
    _ = patronHoldPost.setRecordType("i")
    _ = patronHoldPost.setRecordNumber(sierraItemIdentifier.value.toLong)
    _ = patronHoldPost.setPickupLocation(itemLocation.id)

    _ = patronsApi.placeANewHoldRequest(patronHoldPost, userIdentifier.value.toInt)
  } yield ()

  def requestHoldOnItem(
    userIdentity: StacksUserIdentifier,
    catalogueItemId: CatalogueItemIdentifier
  ): Future[StacksHoldRequest] = for {
    itemIdentifier <- getStacksItemIdentifierFrom(catalogueItemId)
    _ <- placeHoldInSierra(userIdentity, itemIdentifier.sierraId)
  } yield StacksHoldRequest(
      itemId = catalogueItemId.value,
      userId = userIdentity.value
    )
}
