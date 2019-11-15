package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.stacks.common.services.StacksWorkService

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

trait ItemsApi extends Logging with FailFastCirceSupport {

  import akka.http.scaladsl.server.Directives._
  import io.circe.generic.auto._

  implicit val ec: ExecutionContext
  implicit val stacksWorkService: StacksWorkService

  val routes: Route = concat(
    pathPrefix("works") {
      path(Segment) { id: String =>
        get {
          val result = stacksWorkService.getItems(id)

          result match {
            case Success(stacksWork) => complete(stacksWork)
            case Failure(err) => failWith(err)
          }
        }
      }
    }
  )
}
