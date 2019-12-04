package uk.ac.wellcome.platform.stacks.common.models

import java.time.Instant

case class StacksUserHolds[ItemId <: Identifier](
                            userId: String,
                            holds: List[StacksHold[ItemId]]
                          ) {
  def updateHolds[DifferentItemId <: Identifier](
                                                      holds: List[StacksHold[DifferentItemId]]
                                                    ): StacksUserHolds[DifferentItemId] =
    StacksUserHolds[DifferentItemId](
      userId = this.userId,
      holds = holds
    )

}

case class StacksHold[ItemId <: Identifier](
                       itemId: ItemId,
                       pickup: StacksPickup,
                       status: StacksHoldStatus,
                     ) {
  def updateItemId[DifferentItemId <: Identifier](itemId: DifferentItemId): StacksHold[DifferentItemId] = {
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
                         pickUpBy: Option[Instant]
                       )