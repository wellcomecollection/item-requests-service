package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext

trait RequestsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext

  val routes: Route = concat(
    pathPrefix("requests") {
      get {
        complete("ok")
      }
    }
  )
}
