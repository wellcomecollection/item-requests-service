package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext
import uk.ac.wellcome.json.JsonUtil._
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi

case class Root(status: String = "ok")
case class TestResponse(workId: String, itemId: String)

trait RequestsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: SierraApi

  val routes: Route = concat(
    pathSingleSlash {
      complete(Root())
    },

    path("healthcheck") {
      get {
        complete("ok")
      }
    }
  )
}
