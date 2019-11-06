package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.items.api.fixtures.ItemsApiFixture


class ItemsApiFeatureTest
  extends FunSpec
    with Matchers
    with ItemsApiFixture
    with JsonAssertions
    with IntegrationPatience {

  describe("requests") {
    it("shows a user their requested items") {
      withConfiguredApp() {
        case (_, _) =>
          val path = "/works/a2239muq"

          whenGetRequestReady(path) { response =>
            response.status shouldBe StatusCodes.OK
          }
      }
    }
  }
}
