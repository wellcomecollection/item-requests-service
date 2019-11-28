package uk.ac.wellcome.platform.stacks.common.models

import uk.ac.wellcome.platform.sierra.models.Hold

sealed trait Identifier {
  val value: String
}

case class SierraItemIdentifier(value: String) extends Identifier

object SierraItemIdentifier {
  def apply(hold: Hold): SierraItemIdentifier = {
    SierraItemIdentifier(
      hold.getRecord.split("/").last
    )
  }
}

case class CatalogueItemIdentifier(value: String) extends Identifier

case class StacksItemIdentifier(
                                 catalogueId: CatalogueItemIdentifier,
                                 sierraId: SierraItemIdentifier
                               )  extends Identifier {
  override val value: String = catalogueId.value
}

case class StacksWorkIdentifier(value: String) extends Identifier