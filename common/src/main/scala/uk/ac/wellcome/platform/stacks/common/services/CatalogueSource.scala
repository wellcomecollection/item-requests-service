package uk.ac.wellcome.platform.stacks.common.services

import uk.ac.wellcome.platform.stacks.common.models.{Identifier, StacksWorkIdentifier}

import scala.concurrent.Future

trait CatalogueSource {
  import CatalogueSource._

  def getWorkStub(id: StacksWorkIdentifier): Future[WorkStub]
  def getSearchStub(identifier: Identifier[_]): Future[SearchStub]
}

object CatalogueSource {
  case class TypeStub(
                       id: String,
                       label: String
                     )

  case class LocationStub(
                           locationType: TypeStub,
                           label: Option[String],
                           `type`: String
                         )

  case class IdentifiersStub(
                              identifierType: TypeStub,
                              value: String
                            )

  case class ItemStub(
                       id: String,
                       identifiers: List[IdentifiersStub],
                       locations: List[LocationStub]
                     )

  case class WorkStub(
                       id: String,
                       items: List[ItemStub]
                     )

  case class SearchStub(
                         totalResults: Int,
                         results: List[WorkStub]
                       )
}
