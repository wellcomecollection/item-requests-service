package uk.ac.wellcome.platform.stacks.common.models

case class StacksItem(
                       id: String,
                       location: StacksLocation,
                       status: StacksItemStatus
                     )