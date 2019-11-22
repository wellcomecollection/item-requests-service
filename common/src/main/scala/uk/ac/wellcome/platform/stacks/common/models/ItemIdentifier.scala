package uk.ac.wellcome.platform.stacks.common.models

import uk.ac.wellcome.platform.sierra.models.Hold

sealed trait ItemIdentifier {
  val value: String
}

case class SierraItemIdentifier(value: String) extends ItemIdentifier

object SierraItemIdentifier {
  def apply(hold: Hold): SierraItemIdentifier = {
    SierraItemIdentifier(
      hold.getRecord.split("/").last
    )
  }
}

case class CatalogueItemIdentifier(value: String) extends ItemIdentifier

case class StacksItemIdentifier(
                                 catalogueId: CatalogueItemIdentifier,
                                 sierraId: SierraItemIdentifier
                               )  extends ItemIdentifier {
  override val value: String = catalogueId.value
}
