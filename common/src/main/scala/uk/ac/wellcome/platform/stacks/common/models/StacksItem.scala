package uk.ac.wellcome.platform.stacks.common.models

case class StacksItem(
                       itemId: String,
                       location: StacksLocation,
                       status: StacksItemStatus
                     )