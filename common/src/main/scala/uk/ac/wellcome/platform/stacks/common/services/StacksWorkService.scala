package uk.ac.wellcome.platform.stacks.common.services

import uk.ac.wellcome.platform.stacks.common.models.{StacksItem, StacksItemStatus, StacksLocation, StacksWork}

import scala.collection.JavaConverters._
import scala.util.Try
import com.google.gson.internal.LinkedTreeMap
import uk.ac.wellcome.platform.catalogue.api.WorksApi

import scala.concurrent.{Await, ExecutionContext, Future}

import scala.concurrent._
import scala.concurrent.duration._

class StacksWorkService(worksApi: WorksApi, sierraService: SierraService)(
                       implicit ec: ExecutionContext
) {
  type LTM[T] = LinkedTreeMap[String, T]

  implicit class LinkedTreeMapExtractor(ltm: LTM[Any]) {
    def safeGet[T](key: String): Option[T] =
      Option(ltm.get(key)).flatMap(o => Try(o.asInstanceOf[T]).toOption)
  }

  def getItems(workId: String) = Try {

    val work = worksApi.getWork(workId, "items,identifiers")

    StacksWork(
      workId = work.getId,
      items = work.getItems.asScala.toList.map { item =>

        val itemIdentifiers = item
          .getIdentifiers
          .asScala.toList

        val identifiers = itemIdentifiers
          .filter { _.getIdentifierType.getId == "sierra-identifier" }
          .map { _.getValue }

        val sierraIdentifier = identifiers.head

        val itemLocations = item
          .getLocations
          .asScala.toList

        val locations = itemLocations
          .map(_.asInstanceOf[LTM[Any]])
          .map { l =>
            (
              l.safeGet[LTM[String]]("locationType").map(_.get("id")),
              l.safeGet[String]("type"),
              l.safeGet[String]("label")
            )
          }
          .collect {
            case (Some(id), Some("PhysicalLocation"), Some(label)) =>
              StacksLocation(id,label)
          }

        val futureStatus = sierraService.itemsApi().map { api =>
          val sierraItem = api.getAnItemByRecordID(sierraIdentifier, List.empty[String].asJava)
          val itemStatus = sierraItem.getStatus()

          StacksItemStatus("nope", itemStatus.getDisplay)
        }

        val status = Await.result(futureStatus, 5 seconds)

        StacksItem(
          itemId = item.getId,
          location = locations.head,
          status = status
        )
      }
    )
  }
}