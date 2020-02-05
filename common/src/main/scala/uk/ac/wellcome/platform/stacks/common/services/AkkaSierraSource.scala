package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.stream.ActorMaterializer
import uk.ac.wellcome.platform.stacks.common.models.{SierraItemIdentifier, StacksLocation, StacksUserIdentifier}
import uk.ac.wellcome.platform.stacks.common.services.SierraSource.{SierraHoldRequestPostBody, SierraItemStub, SierraUserHoldsStub}

import scala.concurrent.Future

class AkkaSierraSource(
                        val baseUri: Uri = Uri(
                          "https://libsys.wellcomelibrary.org/iii/sierra-api"
                        ),
                        credentials: BasicHttpCredentials
                      )(
                        implicit
                        val system: ActorSystem,
                        val mat: ActorMaterializer
                      ) extends SierraSource
  with AkkaClientServiceGet
  with AkkaClientServicePost
  with AkkaClientTokenExchange {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  override val tokenPath = Path("v5/token")

  // See https://sandbox.iii.com/iii/sierra-api/swagger/index.html#!/items
  def getSierraItemStub(sierraId: SierraItemIdentifier): Future[SierraItemStub] = for {
    token <- getToken(credentials)
    item <- get[SierraItemStub](
      path = Path(s"v5/items/${sierraId.value}"),
      headers = List(Authorization(token))
    )
  } yield item

  // See https://sandbox.iii.com/iii/sierra-api/swagger/index.html#!/patrons
  def getSierraUserHoldsStub(userId: StacksUserIdentifier): Future[SierraUserHoldsStub] =  for {
    token <- getToken(credentials)

    holds <- get[SierraUserHoldsStub](
      path = Path(s"v5/patrons/${userId.value}/holds"),
      params = Map(
        ("limit", "100"),
        ("offset", "0")
      ),
      headers = List(Authorization(token))
    )
  } yield holds

  // See https://sandbox.iii.com/iii/sierra-api/swagger/index.html#!/patrons
  def postHold(
                 userIdentifier: StacksUserIdentifier,
                 sierraItemIdentifier: SierraItemIdentifier,
                 itemLocation: StacksLocation
               ): Future[Unit] =
    for {
      token <- getToken(credentials)
      _ <- post[SierraHoldRequestPostBody, String](
        path = Path(s"v5/patrons/${userIdentifier.value}/holds/requests"),
        body = Some(
          SierraHoldRequestPostBody(
            recordType = "i",
            recordNumber = sierraItemIdentifier.value,
            pickupLocation = itemLocation.id
          )
        ),
        headers = List(Authorization(token))
      )
    } yield ()
}
