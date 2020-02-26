package uk.ac.wellcome.platform.stacks.common.fixtures

import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource.{
  IdentifiersStub,
  ItemStub,
  LocationStub,
  TypeStub
}

import scala.util.Random

trait CatalogueServiceFixtures {

  def createPhysicalLocation(
    id: String = Random.nextString(10),
    label: String = Random.nextString(10)
  ) = LocationStub(
    locationType = TypeStub(
      id = id,
      label = label
    ),
    label = Some(label),
    `type` = "PhysicalLocation"
  )

  def createItem(
    catId: String = Random.nextString(10),
    sierraId: Long = Random.nextLong(),
    locations: List[LocationStub] = List(createPhysicalLocation())
  ) = ItemStub(
    id = Some(catId),
    identifiers = Some(
      List(
        IdentifiersStub(
          identifierType = TypeStub(
            id = "sierra-identifier",
            label = "Sierra identifier"
          ),
          value = sierraId.toString
        )
      )
    )
  )
}
