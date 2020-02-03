package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.stream.ActorMaterializer
import uk.ac.wellcome.platform.stacks.common.models._

import scala.concurrent.Future

class SierraService(val baseUri: Uri = Uri(
  "https://libsys.wellcomelibrary.org/iii/sierra-api"
), credentials: BasicHttpCredentials)(
  implicit
    val system: ActorSystem,
    val mat: ActorMaterializer
) extends AkkaClientService
    with AkkaClientServiceGet
    with AkkaClientServicePost
    with AkkaClientTokenExchange {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  override val tokenPath = "v5/token"

  def getItemStatus(sierraId: SierraItemIdentifier): Future[StacksItemStatus] =
    for {
      token <- getToken(credentials)
      item <- get[SierraItemStub](
        path = s"v5/items/$sierraId",
        headers = List(Authorization(token))
      )

    } yield StacksItemStatus(item.status.code)

  def placeHold(
    userIdentifier: StacksUser,
    sierraItemIdentifier: SierraItemIdentifier,
    itemLocation: StacksLocation
  ): Future[Unit] =
    for {
      token <- getToken(credentials)
      _ <- post[SierraHoldRequestPostBody, String](
        path = s"v5/patrons/${userIdentifier.value}/holds/requests",
        body = Some(SierraHoldRequestPostBody(
          recordType = "i",
          recordNumber = sierraItemIdentifier.value,
          pickupLocation = itemLocation.id
        )),
        headers = List(Authorization(token))
      )
    } yield ()

  protected def buildStacksHold(entry: SierraUserHoldsEntryStub): StacksHold[SierraItemIdentifier] = {

    val itemId = SierraItemIdentifier
      .createFromString(entry.record)

    val pickupLocation = StacksLocation(
      id = entry.pickupLocation.code,
      label = entry.pickupLocation.name
    )

    val pickup = StacksPickup(
      location = pickupLocation,
      pickUpBy = entry.pickupByDate
    )

    val status = StacksHoldStatus(
      id = entry.status.code,
      label = entry.status.name
    )

    StacksHold(itemId, pickup, status)
  }

  def getStacksUserHolds(userId: StacksUser): Future[StacksUserHolds[SierraItemIdentifier]] = {
    for {
      token <- getToken(credentials)

      item <- get[SierraUserHoldsStub](
        path = s"v5/patrons/${userId.value}/holds",
        params = Map(
          ("limit", "100"),
          ("offset", "0")
        ),
        headers = List(Authorization(token))
      )

      holds = item.entries.map(buildStacksHold)

    } yield StacksUserHolds(
      userId = userId.value,
      holds = holds
    )
  }

  case class SierraUserHoldsPickupLocationStub(
    code: String,
    name: String
  )
  case class SierraUserHoldsStatusStub(
    code: String,
    name: String
  )
  case class SierraUserHoldsEntryStub(
    id: String, record: String,
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
    pickupLocation: String
  )
}