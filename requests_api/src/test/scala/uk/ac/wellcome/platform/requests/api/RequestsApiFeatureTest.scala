package uk.ac.wellcome.platform.requests.api

import akka.http.scaladsl.model.{HttpEntity, StatusCodes, ContentTypes}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions

class RequestsApiFeatureTest
    extends FunSpec
    with Matchers
    with RequestsApiFixture
    with JsonAssertions
    with IntegrationPatience {

  describe("requests") {
    it("shows a user their requested items") {
      withConfiguredApp() {
        case (metrics, baseUrl) =>
          val expectedJson =
            s"""{ "workId": "12345", "itemId": "67890" }""".stripMargin

          val url =
            s"$baseUrl/works/a2239muq/items/v9m3ewes"

          whenPostRequestReady(url, HttpEntity(ContentTypes.`application/json`, """{ "id" : 1097124 }""")) { response =>
            response.status shouldBe StatusCodes.OK

            withStringEntity(response.entity) { actualJson =>
              assertJsonStringsAreEqual(actualJson, expectedJson)
            }

            assertMetricSent(
              metrics,
              result = HttpMetricResults.Success
            )
          }
      }
    }
  }
}
