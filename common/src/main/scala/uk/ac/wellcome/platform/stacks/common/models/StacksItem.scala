package uk.ac.wellcome.platform.stacks.common.models

sealed trait StacksItem {
  val id: StacksItemIdentifier
  val location: StacksLocation
}

case class StacksItemWithStatus(
                       id: StacksItemIdentifier,
                       location: StacksLocation,
                       status: StacksItemStatus
                     ) extends StacksItem

case class StacksItemWithOutStatus(
                                 id: StacksItemIdentifier,
                                 location: StacksLocation
                               ) extends StacksItem {
  def addStatus(stacksItemStatus: StacksItemStatus) =
    StacksItemWithStatus(
      id = this.id,
      location = this.location,
      status = stacksItemStatus
    )
}