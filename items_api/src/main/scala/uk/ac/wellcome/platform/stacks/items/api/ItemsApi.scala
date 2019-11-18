package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.services.{StacksWorkIdentifier, StacksWorkService}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait ItemsApi extends Logging with FailFastCirceSupport {

  import akka.http.scaladsl.server.Directives._
  import io.circe.generic.auto._

  implicit val ec: ExecutionContext
  implicit val stacksWorkService: StacksWorkService

  val routes: Route = concat(
    pathPrefix("works") {
      path(Segment) { id: String =>
        get {
          val result = stacksWorkService.getStacksWork(
            StacksWorkIdentifier(id)
          )

          onComplete(result) {
            case Success(value) => complete(value)
            case Failure(err) => failWith(err)
          }
        }
      }
    }
  )
}
