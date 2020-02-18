package uk.ac.wellcome.platform.stacks.items.api.display.models

import uk.ac.wellcome.platform.stacks.common.models.{
  StacksItemWithStatus,
  StacksWork
}

object DisplayStacksWork {
  def apply(stacksWork: StacksWork[StacksItemWithStatus]): DisplayStacksWork =
    DisplayStacksWork(
      id = stacksWork.id,
      items = stacksWork.items.map { stacksItem =>
        DisplayStacksItem(
          id = stacksItem.id.value,
          status = DisplayStacksItemStatus(
            id = stacksItem.status.id,
            label = stacksItem.status.label
          )
        )
      }
    )
}

case class DisplayStacksItemStatus(
  id: String,
  label: String,
  `type`: String = "ItemStatus"
)
case class DisplayStacksItem(
  id: String,
  status: DisplayStacksItemStatus,
  `type`: String = "Item"
)
case class DisplayStacksWork(
  id: String,
  items: List[DisplayStacksItem],
  `type`: String = "Work"
)
