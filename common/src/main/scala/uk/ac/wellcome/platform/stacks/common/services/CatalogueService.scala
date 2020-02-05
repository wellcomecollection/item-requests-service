package uk.ac.wellcome.platform.stacks.common.services
import uk.ac.wellcome.platform.stacks.common.models.{StacksItem, _}
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CatalogueService(
    val catalogueSource: CatalogueSource
)(implicit ec: ExecutionContext) {

  import CatalogueSource._

  protected def getIdentifier(
      identifiers: List[IdentifiersStub]
  ): Option[SierraItemIdentifier] =
    identifiers filter (_.identifierType.id == "sierra-identifier") match {
      case List(IdentifiersStub(_, value)) =>
        Try(value.toLong) match {
          case Success(l) => Some(SierraItemIdentifier(l))
          case Failure(_) =>
            throw new Exception(
              s"Unable to convert $value to Long!"
            )
        }

      case _ => None
    }

  protected def getLocations(
      locations: List[LocationStub]
  ): List[StacksLocation] = locations collect {
    case location @ LocationStub(_, _, "PhysicalLocation") =>
      StacksLocation(
        location.locationType.id,
        location.locationType.label
      )
  }

  protected def getStacksItems(
      itemStubs: List[ItemStub]
  ): List[StacksItemWithOutStatus] =
    itemStubs map {
      case ItemStub(id, identifiers, locations) =>
        (
          CatalogueItemIdentifier(id),
          getIdentifier(identifiers),
          getLocations(locations)
        )
    } map {
      case (catId, Some(sierraId), List(location)) =>
        StacksItemWithOutStatus(
          StacksItemIdentifier(catId, sierraId),
          location
        )
    }

  def getStacksWork(
      workId: StacksWorkIdentifier
  ): Future[StacksWork[StacksItemWithOutStatus]] =
    for {
      workStub <- catalogueSource.getWorkStub(workId)
      items = getStacksItems(workStub.items)
    } yield StacksWork(workStub.id, items)

  def getStacksItem(identifier: Identifier[_]): Future[Option[StacksItem]] =
    for {
      searchStub <- catalogueSource.getSearchStub(identifier)

      items = searchStub.results
        .map(_.items)
        .flatMap(getStacksItems)

    } yield items match {
      case List(item) => Some(item)
      case _          => None
    }
}
