package uk.ac.wellcome.platform.stacks.common.services

import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.stacks.common.models._

import scala.concurrent.{ExecutionContext, Future}


class StacksService(
                     catalogueService: CatalogueService,
                     sierraService: SierraService
                   )(
                     implicit ec: ExecutionContext
                   ) extends Logging {

  def requestHoldOnItem(
                         userIdentifier: StacksUser,
                         catalogueItemId: CatalogueItemIdentifier
                       ): Future[StacksHoldRequest] = for {
    stacksItem <- catalogueService.getStacksItem(catalogueItemId)

    holdRequest <- stacksItem match {
      case Some(item) => sierraService.placeHold(
        SierraHoldRequest(
          userIdentifier = userIdentifier,
          sierraItemIdentifier = item.id.sierraId,
          itemLocation = item.location
        )
      )
      case None => Future.failed(
        new Exception(f"Could not locate item $catalogueItemId!")
      )
  }
    _ <- holdRequest match {
      case Right(_) =>
        Future.successful(())
      case Left(_: HoldAlreadyExists) =>
        Future.successful(())
      case Left(sierraError: UnknownSierraServiceError) =>
        Future.failed(sierraError.e)
    }

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

  def getStacksUserHolds(
                                                  userId: StacksUser
                                                ): Future[StacksUserHolds[StacksItemIdentifier]] =
    for {
      userHolds <- sierraService.getStacksUserHolds(userId)
      stacksItems <- userHolds.holds
        .map(_.itemId)
        .traverse(catalogueService.getStacksItem)

      updatedUserHolds = (userHolds.holds zip stacksItems) map {
        case (hold, Some(stacksItem)) =>
          Some(hold.updateItemId[StacksItemIdentifier](stacksItem.id))
        case (hold, None) => {
          error(f"Unable to map $hold to Catalogue Id!")

          None
        }
      }

    } yield userHolds.updateHolds(updatedUserHolds.flatten)
}
