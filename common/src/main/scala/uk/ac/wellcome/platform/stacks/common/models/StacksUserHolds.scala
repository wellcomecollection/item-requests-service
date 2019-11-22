package uk.ac.wellcome.platform.stacks.common.models

import java.time.Instant

case class StacksUserHolds[ItemId <: ItemIdentifier](
                            userId: String,
                            holds: List[StacksHold[ItemId]]
                          ) {
  def updateHolds[DifferentItemId <: ItemIdentifier](
                                                      holds: List[StacksHold[DifferentItemId]]
                                                    ): StacksUserHolds[DifferentItemId] =
    StacksUserHolds[DifferentItemId](
      userId = this.userId,
      holds = holds
    )

}

case class StacksHold[ItemId <: ItemIdentifier](
                       itemId: ItemId,
                       pickup: StacksPickup,
                       status: StacksHoldStatus,
                     ) {
  def updateItemId[DifferentItemId <: ItemIdentifier](itemId: DifferentItemId): StacksHold[DifferentItemId] = {
    StacksHold[DifferentItemId](
      itemId = itemId,
      pickup = this.pickup,
      status = this.status
    )
  }
}

case class StacksHoldStatus(
                             id: String,
                             label: String
                           )

case class StacksPickup(
                         location: StacksLocation,
                         pickUpBy: Instant
                       )