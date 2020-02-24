package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import uk.ac.wellcome.platform.stacks.common.models._
import uk.ac.wellcome.platform.stacks.common.services.source.SierraSource

import scala.concurrent.{ExecutionContext, Future}

class SierraService(
    sierraSource: SierraSource
)(implicit ec: ExecutionContext) {

  import SierraSource._

  def getItemStatus(sierraId: SierraItemIdentifier): Future[StacksItemStatus] =
    sierraSource
      .getSierraItemStub(sierraId)
      .map(item => StacksItemStatus(item.status.code))

  def placeHold(
      userIdentifier: StacksUserIdentifier,
      sierraItemIdentifier: SierraItemIdentifier,
      neededBy: Option[Instant]
  ): Future[Unit] =
    sierraSource
      .postHold(
        userIdentifier,
        sierraItemIdentifier,
        neededBy
      )

  protected def buildStacksHold(
      entry: SierraUserHoldsEntryStub
  ): StacksHold[SierraItemIdentifier] = {

    val itemId = SierraItemIdentifier
      .createFromSierraId(entry.record)

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

  def getStacksUserHolds(
      userId: StacksUserIdentifier
  ): Future[StacksUserHolds[SierraItemIdentifier]] = {
    sierraSource
      .getSierraUserHoldsStub(userId)
      .map { hold =>
        StacksUserHolds(
          userId = userId.value,
          holds = hold.entries.map(buildStacksHold)
        )
      }
  }
}
