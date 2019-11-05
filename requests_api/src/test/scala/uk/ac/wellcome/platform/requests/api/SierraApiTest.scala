package uk.ac.wellcome.platform.requests.api

import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions

class SierraApiTest extends FunSpec with Matchers with JsonAssertions {

    val patronId = "1097124"
    val otherPatronId = "3133361"
    val itemId = "1786940"
    val user = sys.env.getOrElse("SIERRA_USER", "key")
    val pass = sys.env.getOrElse("SIERRA_PASS", "secret")
    val api = new HttpSierraApi(SierraApiConfig(
      user,
      pass
    ))

    describe("all the methods" ) {
      it("getItem") {
        api.getItem(itemId)
      }

      it("getPatron") {
        api.getPatron(otherPatronId)
      }

      it("getPatronHolds") {
        println(api.getPatronHolds(patronId))
      }

      it("deletePatronHolds") {
        api.deletePatronHolds(patronId)
      }

      it("postPatronPlaceHold") {
        api.postPatronPlaceHold(patronId, itemId)
        api.deletePatronHolds(patronId)
      }

      it ("patron") {
        api.getPatron(patronId)
      }
    }

}
