package uk.ac.wellcome.platform.stacks.items.api.display.models

import uk.ac.wellcome.platform.stacks.common.models.{StacksItemWithStatus, StacksWork}

object DisplayStacksWork {
  def apply(stacksWork: StacksWork[StacksItemWithStatus]): DisplayStacksWork = DisplayStacksWork(
    id = stacksWork.id,
    items = stacksWork.items.map { stacksItem =>
      DisplayStacksItem(
        id = stacksItem.id.value,
        location = DisplayStacksLocation(
          id = stacksItem.location.id,
          label = stacksItem.location.label
        ),
        status = DisplayStacksItemStatus(
          id = stacksItem.status.id,
          label = stacksItem.status.label
        )
      )
    }
  )
}

case class DisplayStacksLocation(id: String, label: String)
case class DisplayStacksItemStatus(id: String, label: String)
case class DisplayStacksItem(id: String, location: DisplayStacksLocation, status: DisplayStacksItemStatus)
case class DisplayStacksWork(id: String, items: List[DisplayStacksItem])
