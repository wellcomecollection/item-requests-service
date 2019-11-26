package uk.ac.wellcome.platform.stacks.common.fixtures

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import uk.ac.wellcome.fixtures.TestWith

trait CatalogueWireMockFixture {
  def withMockCatalogueServer[R](
    testWith: TestWith[String, R]
  ): R = {

    val wireMockServer = new WireMockServer(
      WireMockConfiguration
        .wireMockConfig()
        .usingFilesUnderClasspath("./common/src/test/resources/catalogue")
        .dynamicPort()
    )

    wireMockServer.start()

    val result = testWith(s"http://localhost:${wireMockServer.port()}")

    wireMockServer.shutdown()

    result
  }
}
