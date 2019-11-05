package uk.ac.wellcome.platform.status.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext
import uk.ac.wellcome.json.JsonUtil._
import uk.ac.wellcome.platform.stacks.common.catalogue.services.CatalogueApi
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi

case class TestResponse(workId: String, itemId: String)
trait StatusApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: SierraApi

  val routes: Route = concat(
    path("works" / Segment / "items" / Segment) {
      case (_, itemId) =>
        get {
          val sierraItemNumber = CatalogueApi.getItemINumber(itemId)
          val item = sierraApi.getItem(sierraItemNumber.get)
          complete(item)
        }
    }
  )
}
