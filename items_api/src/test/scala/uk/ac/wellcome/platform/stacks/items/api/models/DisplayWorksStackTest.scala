package uk.ac.wellcome.platform.stacks.items.api.display.models

import io.circe.syntax._
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.common.models.{
  CatalogueItemIdentifier,
  SierraItemIdentifier,
  StacksItem,
  StacksItemIdentifier,
  StacksItemStatus,
  StacksItemWithOutStatus,
  StacksItemWithStatus,
  StacksLocation,
  StacksWork
}
import uk.ac.wellcome.platform.stacks.items.api.fixtures.ItemsApiFixture

class ItemsApiFeatureTest
    extends FunSpec
    with Matchers
    with ItemsApiFixture
    with JsonAssertions
    with StacksJsonSerialisers {

  describe("serialises StacksWorks correctly") {
    it("shows a user the items on a work") {
      val stacksWork =
        StacksWork(
          id = "cnkv77md",
          items = List(
            StacksItemWithStatus(
              id = StacksItemIdentifier(
                catalogueId = CatalogueItemIdentifier(value = "ys3ern6x"),
                sierraId = SierraItemIdentifier(value = "14248517")),
              location = StacksLocation(id = "sicon",
                                        label = "Closed stores Iconographic"),
              status = StacksItemStatus(rawCode = "-")
            ))
        )

      val expectedJson =
        s"""
           |{
           |  "id" : "cnkv77md",
           |  "items" : [
           |    {
           |      "id" : "ys3ern6x",
           |      "location" : {
           |        "id" : "sicon",
           |        "label" : "Closed stores Iconographic"
           |      },
           |      "status" : {
           |        "id" : "available",
           |        "label" : "Available"
           |      }
           |    }
           |  ]
           |}""".stripMargin

      assertJsonStringsAreEqual(stacksWork.asJson.toString(), expectedJson)
    }
  }
}

trait StacksJsonSerialisers {
  import io.circe._, io.circe.generic.semiauto._
  import io.circe.syntax._
  case class DisplayStacksItem(id: String,
                               location: StacksLocation,
                               status: StacksItemStatus)

  implicit val stacksWorkWithStatusEncoder
    : Encoder[StacksWork[StacksItemWithStatus]] =
    deriveEncoder[StacksWork[StacksItemWithStatus]]

  implicit val stacksLocationEncoder: Encoder[StacksLocation] =
    deriveEncoder[StacksLocation]

  implicit val stacksItemStatusEncoder: Encoder[StacksItemStatus] =
    deriveEncoder[StacksItemStatus]

  implicit val displayStacksItemEncoder: Encoder[DisplayStacksItem] =
    deriveEncoder[DisplayStacksItem]

  implicit val encodeStacksItem: Encoder[StacksItemWithStatus] =
    new Encoder[StacksItemWithStatus] {
      final def apply(stacksItem: StacksItemWithStatus): Json = {

        DisplayStacksItem(stacksItem.id.catalogueId.value,
                          stacksItem.location,
                          stacksItem.status).asJson
      }
    }
}
