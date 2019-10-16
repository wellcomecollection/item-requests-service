package uk.ac.wellcome.platform.requests.api

import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.requests.api.config.models.SierraApiConfig

class SierraApiTest extends FunSpec with Matchers with JsonAssertions {


    val api = new HttpSierraApi(SierraApiConfig(
      "hello",
      "123"
    ))

    it("patron holds") {
      println(api.getPatronHolds(1097124))
    }

    it("delete patron holds") {
      println(api.deletePatronHold(1097124))
      println(api.deletePatronHold(1424851))
      println(api.deletePatronHold(143970))
    }
}
