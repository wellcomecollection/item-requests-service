package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.items.api.fixtures.{CatalogueWireMockFixture, ItemsApiFixture}


class ItemsApiFeatureTest
  extends FunSpec
    with Matchers
    with ItemsApiFixture
    with JsonAssertions
    with CatalogueWireMockFixture
    with IntegrationPatience {

  describe("items") {
    it("shows a user the items on a work") {
      withMockCatalogueServer { apiUrl: String =>
        withConfiguredApp(apiUrl) {
          case (_, _) =>

            val path = "/works/cnkv77md"

            val expectedJson =
              s"""{
                 |  "workId" : "cnkv77md",
                 |  "items" : [
                 |    {
                 |      "itemId" : "ys3ern6x",
                 |      "locationId": "sicon"
                 |      "locationLabel" : "Closed stores Iconographic"
                 |    }
                 |  ]
                 |}"""
                .stripMargin

            whenGetRequestReady(path) { response =>
              response.status shouldBe StatusCodes.OK

              withStringEntity(response.entity) { actualJson =>
                assertJsonStringsAreEqual(actualJson, expectedJson)
              }
            }
        }
      }
    }
  }
}
