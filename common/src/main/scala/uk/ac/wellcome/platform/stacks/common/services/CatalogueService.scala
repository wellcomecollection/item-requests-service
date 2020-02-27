package uk.ac.wellcome.platform.stacks.common.services

import uk.ac.wellcome.platform.stacks.common.models._
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

  protected def getStacksItems(
    itemStubs: List[ItemStub]
  ): List[StacksItemIdentifier] =
    itemStubs collect {
      case ItemStub(Some(id), Some(identifiers)) =>
        (
          CatalogueItemIdentifier(id),
          getIdentifier(identifiers)
        )
    } collect {
      case (catId, Some(sierraId)) =>
        StacksItemIdentifier(catId, sierraId)
    }

  def getStacksItems(
    workId: StacksWorkIdentifier
  ): Future[List[StacksItemIdentifier]] =
    for {
      workStub <- catalogueSource.getWorkStub(workId)
      items = getStacksItems(workStub.items)
    } yield items

  def getStacksItem(
    identifier: Identifier[_]
  ): Future[Option[StacksItemIdentifier]] =
    for {
      searchStub <- catalogueSource.getSearchStub(identifier)

      items = searchStub.results
        .map(_.items)
        .flatMap(getStacksItems)

      // Ensure we are only matching items that match the passed id!
      filteredItems = identifier match {
        case CatalogueItemIdentifier(id) =>
          items.filter(_.catalogueId.value == id)
        case SierraItemIdentifier(id) =>
          items.filter(_.sierraId.value == id)
        case StacksWorkIdentifier(id) =>
          items.filter(_.catalogueId.value == id)
        case _ => items
      }

      // Items can appear on multiple works in a search result
      distinctFilteredItems = filteredItems.distinct

    } yield distinctFilteredItems match {
      case List(item) => Some(item)
      case Nil        => None
      case _ =>
        throw new Exception(
          s"Found multiple matching items for $identifier in: $distinctFilteredItems"
        )
    }
}
