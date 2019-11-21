package uk.ac.wellcome.platform.stacks.common.models

import uk.ac.wellcome.platform.stacks.common.services.StacksItemIdentifier

case class StacksItem(
                       id: StacksItemIdentifier,
                       location: StacksLocation,
                       status: Option[StacksItemStatus] = None
                     )