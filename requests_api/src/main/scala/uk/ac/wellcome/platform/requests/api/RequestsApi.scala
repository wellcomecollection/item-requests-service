package uk.ac.wellcome.platform.requests.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext
import uk.ac.wellcome.json.JsonUtil._

case class Root(status: String = "ok")
case class TestResponse(workId: String, itemId: String)
trait RequestsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: HttpSierraApi

  val routes: Route = concat(
    pathSingleSlash {
      complete(Root())
    },
//    path("members" / Segment) { memberId =>
//      val holds = sierraApi.getPatronHolds(memberId)
//      complete(holds)
//    },
//    path("members" / Segment) { memberId =>
//      delete {
//        sierraApi.deletePatronHolds(memberId)
//        complete("""{ "status": "success" }""")
//      }
//    },
    path("works" / Segment / "items" / Segment) {
      case (_, itemId) =>
        post {
          entity(as[SierraPatron]) { patron =>
            val sierraItemNumber = HttpCatalogueApi.getItemINumber(itemId)
            val holdRequest = sierraApi.postPatronPlaceHold(patron.id.toString, sierraItemNumber.get)
            complete(holdRequest)
          }
        }
    }
  )
}
