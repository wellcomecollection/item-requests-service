package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi

import scala.concurrent.ExecutionContext

case class TestResponse(workId: String, itemId: String)

trait ItemsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: SierraApi

  val routes: Route = concat(
    pathPrefix("works") {
      path(Segment) { _: String =>
        get {
          complete("ok")
        }
      }
    }
  )
}
