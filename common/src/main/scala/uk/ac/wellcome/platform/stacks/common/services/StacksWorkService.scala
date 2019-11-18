package uk.ac.wellcome.platform.stacks.common.services

import com.google.gson.internal.LinkedTreeMap
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.catalogue.models.{ItemIdentifiers, ResultListItems}
import uk.ac.wellcome.platform.stacks.common.models.{StacksItem, StacksItemStatus, StacksLocation, StacksWork}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


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

  protected def getItemStatus(itemIdentity: StacksItemIdentifier): Future[StacksItemStatus] = for {
    itemsApi <- sierraService.itemsApi()
    sierraItem = itemsApi.getAnItemByRecordID(
      itemIdentity.sierraId,
      List.empty[String].asJava
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
        case (itemId, Some(location)) => getItemStatus(itemId).map { status =>
          StacksItem(itemId.catalogueId, location, status)
        }
      }
    } yield StacksWork(workId.value, stacksItems)
  }
}