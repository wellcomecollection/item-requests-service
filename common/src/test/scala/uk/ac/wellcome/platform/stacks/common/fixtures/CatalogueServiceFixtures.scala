package uk.ac.wellcome.platform.stacks.common.fixtures

import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource.{
  IdentifiersStub,
  ItemStub,
  TypeStub
}

import scala.util.Random

trait CatalogueServiceFixtures {
  def createItem(
    catId: String = Random.nextString(10),
    sierraId: Long = Random.nextLong()
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
