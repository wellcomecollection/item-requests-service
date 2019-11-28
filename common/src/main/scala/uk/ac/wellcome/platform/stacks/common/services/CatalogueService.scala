package uk.ac.wellcome.platform.stacks.common.services

import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import com.google.gson.internal.LinkedTreeMap
import uk.ac.wellcome.platform.catalogue
import uk.ac.wellcome.platform.catalogue.models.{ItemIdentifiers, ResultListItems}
import uk.ac.wellcome.platform.stacks.common.models._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CatalogueService(baseUrl: Option[String])(
  implicit
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

  private def getItems(identifier: Identifier): Future[List[StacksItemWithOutStatus]] = {
    for {
      items <- identifier match {
        case StacksWorkIdentifier(workId) => Future {
          val work = worksApi.getWork(workId, "items,identifiers")
          work.getItems.asScala.toList
        }
        case _ => Future {
          worksApi.getWorks(
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
            identifier.value,
            null,
            null
          ).getResults
            .asScala.toList
            .flatMap(_.getItems.asScala.toList)
        }
      }

      itemIdentifiers <- items.traverse(getStacksItemIdentifierFrom)
      itemLocations <- items.traverse(getStacksLocationFrom)

      stacksItems <- (itemIdentifiers zip itemLocations) traverse {
        case (identifier, Some(location)) => Future.successful(
          StacksItemWithOutStatus(identifier, location)
        )
        case (identifier, None) => throw new Exception(
          f"Missing location for item: $identifier"
        )
      }

    } yield identifier match {

      case StacksWorkIdentifier(_) =>
        stacksItems

      case id@SierraItemIdentifier(_) =>
        stacksItems.filter(_.id.sierraId == id)

      case id@CatalogueItemIdentifier(_) =>
        stacksItems.filter(_.id.catalogueId == id)

      case id@StacksItemIdentifier(_,_) =>
        stacksItems.filter(_.id == id)

    }
  }

  def getStacksWork(workId: StacksWorkIdentifier): Future[StacksWork[StacksItemWithOutStatus]] = for {
    items <- getItems(workId)
  } yield StacksWork(
    id = workId.value,
    items = items
  )

  def getStacksItem(identifier: Identifier): Future[StacksItem] = {
    for {
      items <- getItems(identifier)
    } yield items match {
      case List(item) => item
      case Nil => throw new Exception(
        f"No item found for: $identifier"
      )
      case default => throw new Exception(
        f"Ambiguous item results found for: $identifier ($default)"
      )
    }
  }
}
