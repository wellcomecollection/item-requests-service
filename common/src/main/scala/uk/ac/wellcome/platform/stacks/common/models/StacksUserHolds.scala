package uk.ac.wellcome.platform.stacks.common.models

import java.time.Instant

import uk.ac.wellcome.platform.stacks.common.services.StacksUserIdentifier

case class StacksUserHolds(
                            userId: String,
                            holds: List[StacksHold]
                          )

case class StacksHoldStatus(
                             id: String,
                             label: String
                           )

case class StacksPickup(
                         location: StacksLocation,
                         pickUpBy: Instant
                       )

case class StacksHold(
                       itemId: String,
                       pickup: StacksPickup,
                       status: StacksHoldStatus,
                     )

