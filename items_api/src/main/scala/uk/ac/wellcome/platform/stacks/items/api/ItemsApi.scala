package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.models.{
  StacksItemWithStatus,
  StacksWork,
  StacksWorkIdentifier
}
import uk.ac.wellcome.platform.stacks.common.services.StacksService
import uk.ac.wellcome.platform.stacks.items.api.display.models.DisplayStacksWork

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait ItemsApi extends Logging with FailFastCirceSupport {

  import akka.http.scaladsl.server.Directives._
  import io.circe.generic.auto._

  implicit val ec: ExecutionContext
  implicit val stacksWorkService: StacksService

  val routes: Route = concat(
    pathPrefix("works") {
      path(Segment) {
        id: String =>
          get {
            val result: Future[StacksWork[StacksItemWithStatus]] =
              stacksWorkService.getStacksWorkWithItemStatuses(
                StacksWorkIdentifier(id)
              )

            onComplete(result) {
              case Success(value) => complete(DisplayStacksWork(value))
              case Failure(err)   => failWith(err)
            }
          }
      }
    }
  )
}
