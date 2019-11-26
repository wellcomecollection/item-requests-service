package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.items.api.fixtures.{
  CatalogueWireMockFixture,
  ItemsApiFixture,
  SierraWireMockFixture
}

class ItemsApiFeatureTest
    extends FunSpec
    with Matchers
    with ItemsApiFixture
    with JsonAssertions
    with CatalogueWireMockFixture
    with SierraWireMockFixture
    with IntegrationPatience {

  describe("items") {
    it("shows a user the items on a work") {
      withMockCatalogueServer { catalogueApiUrl: String =>
        withMockSierraServer { sierraApiUrl: String =>
          withConfiguredApp(catalogueApiUrl, sierraApiUrl) {
            case (_, _) =>
              val path = "/works/cnkv77md"

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
}
