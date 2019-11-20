package uk.ac.wellcome.platform.stacks.requests.api.fixtures

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import uk.ac.wellcome.fixtures.TestWith

trait SierraWireMockFixture {
  def withMockSierraServer[R](
                                  testWith: TestWith[String, R]
                                ): R = {

    val wireMockServer = new WireMockServer(
      WireMockConfiguration
        .wireMockConfig()
        .usingFilesUnderClasspath("./requests_api/src/test/resources/sierra")
        .dynamicPort()
    )

    wireMockServer.start()

    val result = testWith(s"http://localhost:${wireMockServer.port()}")

    wireMockServer.shutdown()

    result
  }
}
