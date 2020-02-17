package uk.ac.wellcome.platform.stacks.common.fixtures

import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource.{IdentifiersStub, ItemStub, LocationStub, TypeStub}
import uk.ac.wellcome.storage.generators.RandomThings

trait CatalogueServiceFixtures extends RandomThings {
  def createPhysicalLocation(
                              id: String = randomAlphanumeric,
                              label: String = randomAlphanumeric
                            ) = LocationStub(
    locationType = TypeStub(
      id = id,
      label = label
    ),
    label = Some(label),
    `type` = "PhysicalLocation"
  )

  def createItem(
                  catId: String = randomAlphanumeric,
                  sierraId: Long = randomInt(0, 1000).toLong,
                  locations: List[LocationStub] = List(createPhysicalLocation())
                ) = ItemStub(
    id = catId,
    identifiers = List(
      IdentifiersStub(
        identifierType = TypeStub(
          id = "sierra-identifier",
          label = "Sierra identifier"
        ),
        value = sierraId.toString
      )
    ),
    locations = locations
  )
}
