package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.common.http.fixtures.WireMockFixture
import uk.ac.wellcome.platform.stacks.items.api.fixtures.ItemsApiFixture


trait CatalogueWireMockFixture extends FunSpec with WireMockFixture {
  lazy val apiUrl = "https://api.wellcomecollection.org/catalogue/v2/"
  lazy val mappingsFolder = "src/test/resources/catalogue/"
  lazy val port = 8080
}

class ItemsApiFeatureTest
  extends FunSpec
    with Matchers
    with ItemsApiFixture
    with JsonAssertions
    with CatalogueWireMockFixture
    with IntegrationPatience {

  describe("items") {
    it("shows a user the items on a work") {
      withConfiguredApp() {
        case (_, _) =>
          val path = "/works/a2239muq"

          val expectedJson =
            s"""{ "workId": "12345", "itemId": "67890" }""".stripMargin

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
