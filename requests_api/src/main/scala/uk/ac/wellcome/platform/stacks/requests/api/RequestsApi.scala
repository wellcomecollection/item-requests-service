package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.services.{StacksUserIdentifier, StacksWorkService}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait RequestsApi extends Logging with FailFastCirceSupport {

  import akka.http.scaladsl.server.Directives._
  import io.circe.generic.auto._

  implicit val ec: ExecutionContext
  implicit val stacksWorkService: StacksWorkService

  val routes: Route = concat(
    pathPrefix("requests") {
      get {
        headerValueByName("Sierra-Patron-Id") { sierraPatronId =>
          val result = stacksWorkService.getStacksUserHolds(
            StacksUserIdentifier(sierraPatronId)
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
