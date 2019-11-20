package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import com.google.gson.internal.LinkedTreeMap
import org.threeten.bp.{ZoneId, ZoneOffset}
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.catalogue.models.{ItemIdentifiers, ResultList, ResultListItems}
import uk.ac.wellcome.platform.sierra.models.{Hold, HoldResultSet}
import uk.ac.wellcome.platform.stacks.common.models.{StacksHold, StacksHoldStatus, StacksItem, StacksItemStatus, StacksLocation, StacksPickup, StacksUserHolds, StacksWork}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


case class StacksUserIdentifier(value: String)
case class StacksWorkIdentifier(value: String)
case class StacksItemIdentifier(catalogueId: String, sierraId: String)

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

  protected def getItemIdentifiers(item: ResultListItems): List[ItemIdentifiers] =
    item.getIdentifiers.asScala.toList

  protected def getItems(workId: StacksWorkIdentifier): Future[List[ResultListItems]] =
    Future {
      worksApi.getWork(workId.value, "items,identifiers")
    }.map(_.getItems.asScala.toList)

  protected def getSierraIdentifier(item: ResultListItems): Future[StacksItemIdentifier] = {
    val identifier = getItemIdentifiers(item)
      .filter { _.getIdentifierType.getId == "sierra-identifier" }
      .map { _.getValue }

    identifier match {
      case List(sierraItemId) =>
        Future.successful(StacksItemIdentifier(item.getId, sierraItemId))
      case _ => Future.failed(new Throwable(
        f"Ambiguous or missing identifier! ($identifier)"
      ))
    }
  }

  protected def getStacksLocation(item: ResultListItems): Future[Option[StacksLocation]] = {
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

  protected def getItemStatus(sierraId: String): Future[StacksItemStatus] = for {
    itemsApi <- sierraService.itemsApi()
    sierraItem = itemsApi.getAnItemByRecordID(
      sierraId, List.empty[String].asJava
    )
  } yield StacksItemStatus(sierraItem.getStatus.getCode)

  def getStacksWork(workId: StacksWorkIdentifier): Future[StacksWork] = {
    val eventuallyIdsAndLocations = for {
      items <- getItems(workId)
      sierraIdentifier <- items.traverse(getSierraIdentifier)
      itemLocations <- items.traverse(getStacksLocation)
    } yield sierraIdentifier zip itemLocations

    for {
      idsAndLocations <- eventuallyIdsAndLocations
      stacksItems <- idsAndLocations.traverse {
        case (itemId, Some(location)) => getItemStatus(itemId.sierraId).map { status =>
          StacksItem(itemId.catalogueId, location, status)
        }
      }
    } yield StacksWork(workId.value, stacksItems)
  }

  // ----

  protected def getStacksItemIdentifier(sierraId: String) = {
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
        f"i$sierraId",
        null,
        null
      ).getResults.asScala.toList

      worksResultList match {
        case headWork :: _ => headWork.getItems.asScala.toList
        case _ => throw new Exception("No items found for work!")
      }
    }

    for {
      items <- eventuallyItems
      sierraItems <- items.traverse(getSierraIdentifier)
    } yield sierraItems
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

  protected def getStacksHoldStatusFromHold(hold: Hold): StacksHoldStatus = {
    StacksHoldStatus(
      id = hold.getStatus.getCode,
      label = hold.getStatus.getName
    )
  }

  protected def getStacksItemIdentifierFromHold(hold: Hold): Future[StacksItemIdentifier] = {
    hold.getRecordType match {
      case "i" => {
        val sierraItemId = hold.getRecord.split("/").last
        getStacksItemIdentifier(sierraItemId).map {
          case List(stacksItemIdentifier) => stacksItemIdentifier
          case _ => throw new Exception(f"Ambiguous or missing item record! ($hold)")
        }
      }
      case _ => Future.failed(
        new Throwable(f"Could not get item record from hold! ($hold)")
      )
    }
  }

  def getStacksUserHolds(userId: StacksUserIdentifier): Future[StacksUserHolds] = for {
    holdResultSet <- getHoldResultSet(userId)
    entries = holdResultSet.getEntries.asScala.toList

    stacksItemsIdentifiers <- entries.traverse(getStacksItemIdentifierFromHold)
    holdStatuses = entries.map(getStacksHoldStatusFromHold)
    stacksPickups = entries.map(getStacksPickupFromHold)

    userHolds = (stacksItemsIdentifiers, holdStatuses, stacksPickups)
      .zipped.toList map {
      case (stacksItemsIdentifier, holdStatus, stacksPickup) =>
            StacksHold(
              itemId = stacksItemsIdentifier.catalogueId,
              pickup = stacksPickup,
              status = holdStatus
            )
    }

  } yield StacksUserHolds(
    userId = userId.value,
    holds = userHolds,
  )

  // ----


}
