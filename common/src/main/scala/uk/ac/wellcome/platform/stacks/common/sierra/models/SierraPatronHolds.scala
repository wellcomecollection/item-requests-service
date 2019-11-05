package uk.ac.wellcome.platform.stacks.common.sierra.models

case class SierraPatronHolds(total: Int, entries: List[SierraPatronHolds.Entry])

object SierraPatronHolds {
  case class Entry(id: String,
                   record: String,
                   patron: String,
                   pickupLocation: SierraItem.SierraLocation,
                   status: Status)

  case class Request(recordType: String,
                     recordNumber: String,
                     pickupLocation: String,
                     note: String)

  case class Status(code: String, name: String)
}