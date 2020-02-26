package uk.ac.wellcome.platform.stacks.common.services.source

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.stream.ActorMaterializer
import io.circe.Encoder
import uk.ac.wellcome.platform.stacks.common.http.{
  AkkaClientGet,
  AkkaClientPost,
  AkkaClientTokenExchange
}
import uk.ac.wellcome.platform.stacks.common.models.{
  SierraItemIdentifier,
  StacksUserIdentifier
}
import uk.ac.wellcome.platform.stacks.common.services.source.SierraSource.{
  SierraHoldRequestPostBody,
  SierraItemStub,
  SierraUserHoldsStub
}

import scala.concurrent.Future

trait SierraSource {
  import SierraSource._

  def getSierraItemStub(sierraId: SierraItemIdentifier): Future[SierraItemStub]
  def getSierraUserHoldsStub(
    userId: StacksUserIdentifier
  ): Future[SierraUserHoldsStub]
  def postHold(
    userIdentifier: StacksUserIdentifier,
    sierraItemIdentifier: SierraItemIdentifier,
    neededBy: Option[Instant]
  ): Future[Unit]
}

object SierraSource {
  case class SierraUserHoldsPickupLocationStub(
    code: String,
    name: String
  )
  case class SierraUserHoldsStatusStub(
    code: String,
    name: String
  )
  case class SierraUserHoldsEntryStub(
    id: String,
    record: String,
    pickupLocation: SierraUserHoldsPickupLocationStub,
    pickupByDate: Option[Instant],
    status: SierraUserHoldsStatusStub
  )
  case class SierraUserHoldsStub(
    total: Long,
    entries: List[SierraUserHoldsEntryStub]
  )
  case class SierraItemStatusStub(
    code: String,
    display: String
  )
  case class SierraItemStub(
    id: String,
    status: SierraItemStatusStub
  )
  case class SierraHoldRequestPostBody(
    recordType: String,
    recordNumber: Long,
    pickupLocation: String,
    neededBy: Option[Instant]
  )
}

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
    with AkkaClientGet
    with AkkaClientPost
    with AkkaClientTokenExchange {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  override val tokenPath = Path("v5/token")

  // See https://sandbox.iii.com/iii/sierra-api/swagger/index.html#!/items
  def getSierraItemStub(
    sierraId: SierraItemIdentifier
  ): Future[SierraItemStub] =
    for {
      token <- getToken(credentials)
      item <- get[SierraItemStub](
        path = Path(s"v5/items/${sierraId.value}"),
        headers = List(Authorization(token))
      )
    } yield item

  // See https://sandbox.iii.com/iii/sierra-api/swagger/index.html#!/patrons
  def getSierraUserHoldsStub(
    userId: StacksUserIdentifier
  ): Future[SierraUserHoldsStub] =
    for {
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

  private val dateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd")
    .withZone(ZoneId.systemDefault())

  implicit val encodeInstant: Encoder[Instant] =
    Encoder.encodeString.contramap[Instant](dateTimeFormatter.format)

  // See https://sandbox.iii.com/iii/sierra-api/swagger/index.html#!/patrons
  def postHold(
    userIdentifier: StacksUserIdentifier,
    sierraItemIdentifier: SierraItemIdentifier,
    neededBy: Option[Instant]
  ): Future[Unit] =
    for {
      token <- getToken(credentials)
      _ <- post[SierraHoldRequestPostBody, String](
        path = Path(s"v5/patrons/${userIdentifier.value}/holds/requests"),
        body = Some(
          SierraHoldRequestPostBody(
            recordType = "i",
            recordNumber = sierraItemIdentifier.value,
            // This field is required non-empty by the Sierra API - but has no effect
            pickupLocation = "unspecified",
            neededBy = neededBy
          )
        ),
        headers = List(Authorization(token))
      )
    } yield ()
}
