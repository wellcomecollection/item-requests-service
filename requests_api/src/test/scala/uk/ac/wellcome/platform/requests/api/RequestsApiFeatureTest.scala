package uk.ac.wellcome.platform.storage.bags.api

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.requests.api.HttpMetricResults
import uk.ac.wellcome.platform.requests.api.fixtures.RequestsApiFixture

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
            s"$baseUrl/works/12345/items/67890"

          whenGetRequestReady(url) { response =>
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
