package uk.ac.wellcome.platform.stacks.common.models

case class StacksWork[Item <: StacksItem](id: String, items: List[Item]) {
  def updateItems[DifferentItem <: StacksItem](
      items: List[DifferentItem]
  ): StacksWork[DifferentItem] =
    StacksWork[DifferentItem](
      id = this.id,
      items = items
    )
}
