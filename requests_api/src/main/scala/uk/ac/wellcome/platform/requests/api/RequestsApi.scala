package uk.ac.wellcome.platform.requests.api

import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext
import uk.ac.wellcome.json.JsonUtil._

case class TestResponse(workId: String, itemId: String)
trait RequestsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: HttpSierraApi

  val routes: Route = concat(
    path("works" / Segment / "items" / Segment) {

      case (_, itemId) =>
        post {
          entity(as[SierraPatron]) { patron =>
            val sierraItemNumber = HttpCatalogueApi.getItemINumber(itemId)
            val holdRequest = sierraApi.postPatronPlaceHold(sierraItemNumber.get.toInt, patron.id)
            complete(holdRequest)
          }
        }
    }
  )
}
