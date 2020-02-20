package uk.ac.wellcome.platform.stacks.requests.api

import java.time.Instant

import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.models.display.DisplayResultsList
import uk.ac.wellcome.platform.stacks.common.models.{CatalogueItemIdentifier, StacksUserIdentifier}
import uk.ac.wellcome.platform.stacks.common.services.StacksService
import uk.ac.wellcome.platform.stacks.requests.api.models.Request

import scala.concurrent.ExecutionContext
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
          val neededBy = Some(
            Instant.parse("2020-01-01T00:00:00.00Z")
          )

          post {
            entity(as[Request]) {
              requestItemHold: Request =>
                val catalogueItemId =
                  CatalogueItemIdentifier(requestItemHold.item.id)

                val result = stacksWorkService.requestHoldOnItem(
                  userIdentifier = userIdentifier,
                  catalogueItemId = catalogueItemId,
                  neededBy = neededBy
                )

                val accepted = (StatusCodes.Accepted, HttpEntity.Empty)

                onComplete(result) {
                  case Success(_)   => complete(accepted)
                  case Failure(err) => failWith(err)
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
