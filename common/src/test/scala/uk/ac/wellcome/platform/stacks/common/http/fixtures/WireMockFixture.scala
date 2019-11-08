package uk.ac.wellcome.platform.stacks.common.http.fixtures

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest._

trait WireMockFixture extends BeforeAndAfterAll with BeforeAndAfterEach { this: Suite =>

  private val host = "localhost"

  val port: Int
  val apiUrl: String
  val mappingsFolder: String

  private val wireMockServer = new WireMockServer(
    WireMockConfiguration
      .wireMockConfig()
      .usingFilesUnderDirectory(mappingsFolder)
      .port(port)
  )

  final val wireMockUrl = s"http://$host:$port"

  wireMockServer.start()

  WireMock.configureFor(host, port)

  override def beforeEach(): Unit = {
    WireMock.reset()

    stubFor(proxyAllTo(apiUrl).atPriority(100))

    super.beforeEach()
  }

  override def afterAll(): Unit = {
    wireMockServer.snapshotRecord(
      recordSpec()
        .forTarget(apiUrl)
        .captureHeader("Authorization")
    )
    wireMockServer.stop()

    super.afterAll()
  }
}
