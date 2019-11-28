package uk.ac.wellcome.platform.stacks.common.services

import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import uk.ac.wellcome.platform.stacks.common.models._

import scala.concurrent.{ExecutionContext, Future}


class StacksService(
                     catalogueService: CatalogueService,
                     sierraService: SierraService
                   )(
                     implicit ec: ExecutionContext
                   ) {

  def requestHoldOnItem(
                         userIdentifier: StacksUser,
                         catalogueItemId: CatalogueItemIdentifier
                       ): Future[StacksHoldRequest] = for {
    stacksItem <- catalogueService.getStacksItem(catalogueItemId)

    _ <- sierraService.placeHold(
      userIdentifier = userIdentifier,
      sierraItemIdentifier = stacksItem.id.sierraId,
      itemLocation = stacksItem.location
    )

  } yield StacksHoldRequest(
    itemId = catalogueItemId.value,
    userId = userIdentifier.value
  )

  def getStacksWorkWithItemStatuses(
                                     workId: StacksWorkIdentifier
                                   ): Future[StacksWork[StacksItemWithStatus]] =
    for {
      stacksWorkWithoutItemStatuses <- catalogueService.getStacksWork(workId)
      itemStatuses <- stacksWorkWithoutItemStatuses.items
        .map(_.id.sierraId)
        .traverse(sierraService.getItemStatus)

      stacksItemsWithStatuses =
      (stacksWorkWithoutItemStatuses.items zip itemStatuses) map {
        case (item, status) => item.addStatus(status)
      }
    } yield stacksWorkWithoutItemStatuses
      .updateItems(stacksItemsWithStatuses)

  def getStacksUserHoldsWithStacksItemIdentifier(
                                                  userId: StacksUser
                                                ): Future[StacksUserHolds[StacksItemIdentifier]] =
    for {
      userHolds <- sierraService.getStacksUserHolds(userId)
      stacksItems <- userHolds.holds
        .map(_.itemId)
        .traverse(catalogueService.getStacksItem)

      updatedUserHolds = (userHolds.holds zip stacksItems) map {
        case (hold: StacksHold[SierraItemIdentifier], stacksItem) =>
          hold.updateItemId[StacksItemIdentifier](stacksItem.id)
      }

    } yield userHolds.updateHolds(updatedUserHolds)
}
