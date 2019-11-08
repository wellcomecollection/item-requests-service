package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext
import uk.ac.wellcome.json.JsonUtil._
import uk.ac.wellcome.platform.stacks.common.catalogue.services.CatalogueApi
import uk.ac.wellcome.platform.stacks.common.sierra.models.SierraPatron
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi

case class Root(status: String = "ok")
case class TestResponse(workId: String, itemId: String)

trait RequestsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: SierraApi

  val routes: Route = concat(

    path("members" / Segment) { memberId =>
      val holds = sierraApi.getPatronHolds(memberId)
      complete(holds)
    },

    path("members" / Segment) { memberId =>
      delete {
        sierraApi.deletePatronHolds(memberId)
        complete("""{ "status": "success" }""")
      }
    },

    path("works" / Segment / "items" / Segment) {
      case (_, itemId) =>
        get {
          val sierraItemNumber = CatalogueApi.getItemNumber(itemId)
          val item = sierraApi.getItem(sierraItemNumber.get)
          complete(item)
        }
        post {
          entity(as[SierraPatron]) { patron =>
            val sierraItemNumber = CatalogueApi.getItemNumber(itemId)
            val holdRequest = sierraApi.postPatronPlaceHold(patron.id, sierraItemNumber.get)
            complete(holdRequest)
          }
        }
    }
  )
}
