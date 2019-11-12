package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.requests.api.fixtures.RequestsApiFixture

class RequestsApiFeatureTest
    extends FunSpec
    with Matchers
    with RequestsApiFixture
    with JsonAssertions
    with IntegrationPatience {

  describe("requests") {
    it("shows a user their requested items") {
      withConfiguredApp() {
        case (_, _) =>
          val path = "/requests"

          whenGetRequestReady(path) { response =>
            response.status shouldBe StatusCodes.OK
          }
      }
    }
  }
}
