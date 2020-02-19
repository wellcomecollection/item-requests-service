package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.models.display.DisplayResultsList
import uk.ac.wellcome.platform.stacks.common.models.{
  CatalogueItemIdentifier,
  StacksHoldRequest,
  StacksItemIdentifier,
  StacksUserHolds,
  StacksUserIdentifier
}
import uk.ac.wellcome.platform.stacks.common.services.StacksService
import uk.ac.wellcome.platform.stacks.requests.api.models.RequestItemHold

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait RequestsApi extends Logging with FailFastCirceSupport {

  import akka.http.scaladsl.server.Directives._
  import io.circe.generic.auto._

  implicit val ec: ExecutionContext
  implicit val stacksWorkService: StacksService

  val routes: Route = concat(
    pathPrefix("requests") {
      headerValueByName("Weco-Sierra-Patron-Id") {
        sierraPatronId =>
          val userIdentifier = StacksUserIdentifier(sierraPatronId)

          post {
            entity(as[RequestItemHold]) {
              requestItemHold: RequestItemHold =>
                val catalogueItemId =
                  CatalogueItemIdentifier(requestItemHold.itemId)

                val result = stacksWorkService.requestHoldOnItem(
                  userIdentifier = userIdentifier,
                  catalogueItemId = catalogueItemId
                )

                onComplete(result) {
                  case Success(value) => complete(value)
                  case Failure(err)   => failWith(err)
                }
            }
          } ~ get {

            val result = stacksWorkService.getStacksUserHolds(
              StacksUserIdentifier(sierraPatronId)
            )

            onComplete(result) {
              case Success(value) => complete(DisplayResultsList(value))
              case Failure(err)   => failWith(err)
            }
          }
      }
    }
  )
}
