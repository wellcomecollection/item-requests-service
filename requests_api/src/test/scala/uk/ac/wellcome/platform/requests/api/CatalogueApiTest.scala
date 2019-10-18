package uk.ac.wellcome.platform.requests.api

import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions


class CatalogueApiTest extends FunSpec with Matchers with JsonAssertions {

    describe("Sierra") {
      it("gets a Sierra Item number from a Catalogue Item ID") {
        val itemNumber = HttpCatalogueApi.getItemINumber("v9m3ewes")
        itemNumber shouldBe Some("1424851")
      }
    }
}
