package uk.ac.wellcome.platform.stacks.common.sierra.models

case class SierraItem(id: String,
                      location: SierraItem.SierraLocation,
                      status: SierraItem.SierraStatus,
                      barcode: String,
                      callNumber: String)

object SierraItem {
  case class SierraLocation(code: String, name: String)
  case class SierraStatus(code: String, display: String)
}
