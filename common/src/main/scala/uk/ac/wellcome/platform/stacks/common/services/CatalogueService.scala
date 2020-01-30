package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import com.google.gson.internal.LinkedTreeMap
import io.circe.Json
import uk.ac.wellcome.platform.catalogue
import uk.ac.wellcome.platform.catalogue.models.{ItemIdentifiers, ResultListItems}
import uk.ac.wellcome.platform.stacks.common.models._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Try

import uk.ac.wellcome.json.JsonUtil._


class CatalogueService2(baseUri: Uri) {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val defaultBaseUri = Uri(
    "https://api.wellcomecollection.org/catalogue/v2"
  )

  def getStacksWork(workId: StacksWorkIdentifier): Future[StacksWork[StacksItemWithOutStatus]] = {
    val urlPath = s"works/${workId.value}"
    val queryParams = "include=items,identifiers"

    val uri = s"${defaultBaseUri}/${urlPath}?${queryParams}"

    case class LocationTypeStub(id: String, label: String)
    case class LocationStub(locationType: LocationTypeStub, label: Option[String], `type`: String)
    case class ItemStub(id: String, locations: List[LocationStub])
    case class WorkStub(id: String, items: List[ItemStub])

    Http().singleRequest(HttpRequest(uri = uri)).flatMap {

      case response@HttpResponse(StatusCodes.OK, _, _, _) =>
        val result = Unmarshal(response).to[WorkStub]

        result.map { workStub =>
          val workStubitems = workStub.items

          val itemsWithPhysicalLocations = workStubitems.filter { item =>
            item.locations.exists(_.`type` == "PhysicalLocation")
          }

          val items = itemsWithPhysicalLocations map { item =>
            val physicalLocations = item.locations.filter {
              _.`type` == "PhysicalLocation"
            }

            val location: LocationStub = physicalLocations.head

            StacksItemWithOutStatus(
              StacksItemIdentifier(
                CatalogueItemIdentifier(item.id),
                SierraItemIdentifier(1)
              ),
              StacksLocation(
                location.locationType.id,
                location.locationType.label)
            )
          }

          StacksWork(
            workStub.id,
            items
          )
        }

    }
  }
}

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
    Option(item.getIdentifiers)
      .map(_.asScala.toList)
      .getOrElse(Nil)

  protected def getStacksItemIdentifierFrom(item: ResultListItems): Future[Option[StacksItemIdentifier]] = {
    val identifier = getItemIdentifiersFrom(item)
      .filter { _.getIdentifierType.getId == "sierra-identifier" }
      .map { _.getValue }

    identifier match {
      case List(sierraItemId) => {
        val catalogueId = CatalogueItemIdentifier(item.getId)
        val sierraId = SierraItemIdentifier
          .createFromString(sierraItemId)

        Future.successful(
          Some(StacksItemIdentifier(catalogueId, sierraId))
        )
      }
      case Nil => Future.successful(None)
      case _ => Future.failed(new Throwable(
        f"Ambiguous identifier! ($identifier)"
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

  private def getItems(identifier: Identifier[_]): Future[List[StacksItemWithOutStatus]] = {
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
            identifier.value.toString,
            null,
            null
          ).getResults
            .asScala.toList
            .flatMap(_.getItems.asScala.toList)
        }
      }

      itemIdentifiers <- items.traverse(getStacksItemIdentifierFrom)
      itemLocations <- items.traverse(getStacksLocationFrom)

      stacksItems = (itemIdentifiers zip itemLocations) flatMap {
        case (Some(identifier), Some(location)) =>
          Some(StacksItemWithOutStatus(identifier, location))
        case _ => None
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

  def getStacksItem(identifier: Identifier[_]): Future[Option[StacksItem]] = {
    for {
      items <- getItems(identifier)
    } yield items match {
      case List(item) => Some(item)
      case Nil => None
      case default => throw new Exception(
        f"Ambiguous item results found for: $identifier ($default)"
      )
    }
  }
}
