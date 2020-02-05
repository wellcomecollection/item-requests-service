package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import uk.ac.wellcome.platform.stacks.common.models.{SierraItemIdentifier, StacksLocation, StacksUserIdentifier}

import scala.concurrent.Future

trait SierraSource {
  import SierraSource._

  def getSierraItemStub(sierraId: SierraItemIdentifier): Future[SierraItemStub]
  def getSierraUserHoldsStub(userId: StacksUserIdentifier): Future[SierraUserHoldsStub]
  def postHold(
                 userIdentifier: StacksUserIdentifier,
                 sierraItemIdentifier: SierraItemIdentifier,
                 itemLocation: StacksLocation
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
                                        pickupLocation: String
                                      )
}
