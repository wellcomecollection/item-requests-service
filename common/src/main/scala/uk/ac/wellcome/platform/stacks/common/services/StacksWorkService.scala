package uk.ac.wellcome.platform.stacks.common.services

import uk.ac.wellcome.platform.catalogue.models.Work
import uk.ac.wellcome.platform.stacks.common.models.{StacksItem, StacksWork}

import collection.JavaConverters._
import scala.util.Try

object StacksWorkService {
  def convertWork(work: Work) = Try {
    val items = work.getItems()

    StacksWork(
      workId = work.getId,
      items = items.asScala.toList.map { item =>
        StacksItem(
          itemId = item.getId,
          locationLabel = "foo"
        )
      }
    )
  }
}