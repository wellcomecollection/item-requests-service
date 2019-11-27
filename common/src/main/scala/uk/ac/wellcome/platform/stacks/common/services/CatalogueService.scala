package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.gson.internal.LinkedTreeMap
import uk.ac.wellcome.platform.catalogue
import uk.ac.wellcome.platform.catalogue.models.{ItemIdentifiers, ResultListItems}
import uk.ac.wellcome.platform.stacks.common.models.{CatalogueItemIdentifier, ItemIdentifier, SierraItemIdentifier, StacksItem, StacksItemIdentifier, StacksItemWithOutStatus, StacksLocation, StacksWork, StacksWorkIdentifier}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._

class CatalogueService(baseUrl: Option[String])(
  implicit
  as: ActorSystem,
  am: ActorMaterializer,
  ec: ExecutionContext
) {
  protected val apiClient = new catalogue.ApiClient()

  baseUrl.foreach { apiClient.setBasePath }

  val worksApi = new catalogue.api.WorksApi(apiClient)

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

  private def getItems(query: String) = Future {
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
      query,
      null,
      null
    ).getResults.asScala.toList

    worksResultList match {
      case headWork :: _ => headWork.getItems.asScala.toList
      case _ => throw new Exception(f"No matching works found for query: ${query}!")
    }
  }

  def getStacksWork(workId: StacksWorkIdentifier): Future[StacksWork[StacksItemWithOutStatus]] = for {
    items <- getItemsFrom(workId)
    itemIdentifiers <- items.traverse(getStacksItemIdentifierFrom)
    itemLocations <- items.traverse(getStacksLocationFrom)

    stacksItems <- (itemIdentifiers zip itemLocations) traverse {
      case (identifier, Some(location)) => Future.successful(
        StacksItemWithOutStatus(identifier, location)
      )
    }
  } yield StacksWork(
    id = workId.value,
    items = stacksItems
  )

  def getStacksItem(identifier: ItemIdentifier): Future[StacksItem] = {

    val identifierString = identifier match {
      case SierraItemIdentifier(value) => f"i${value}"
      case CatalogueItemIdentifier(value) => value
    }

    for {
      items <- getItems(identifierString)
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

    } yield StacksItemWithOutStatus(
      id = itemIdentifier,
      location = stacksLocation
    )
  }
}
