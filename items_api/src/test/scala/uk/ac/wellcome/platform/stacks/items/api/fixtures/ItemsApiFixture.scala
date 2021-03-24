package uk.ac.wellcome.platform.stacks.items.api.fixtures

import com.github.tomakehurst.wiremock.WireMockServer

import java.net.URL
import org.scalatest.concurrent.ScalaFutures
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.monitoring.memory.MemoryMetrics
import uk.ac.wellcome.platform.stacks.common.fixtures.{
  CatalogueWireMockFixture,
  HttpFixtures,
  SierraWireMockFixture
}
import uk.ac.wellcome.platform.stacks.common.http.{HttpMetrics, WellcomeHttpApp}
import uk.ac.wellcome.platform.stacks.common.services.StacksService
import uk.ac.wellcome.platform.stacks.common.services.config.builders.StacksServiceBuilder
import uk.ac.wellcome.platform.stacks.common.services.config.models.{
  CatalogueServiceConfig,
  SierraServiceConfig,
  StacksServiceConfig
}
import uk.ac.wellcome.platform.stacks.items.api.ItemsApi

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait ItemsApiFixture
    extends CatalogueWireMockFixture
    with ScalaFutures
    with HttpFixtures
    with SierraWireMockFixture {

  val metricsName = "ItemsApiFixture"

  val contextURLTest = new URL(
    "http://api.wellcomecollection.org/stacks/v1/context.json"
  )

  private def withApp[R](
    catalogueApiUrl: String,
    sierraApiUrl: String,
    metrics: MemoryMetrics
  )(testWith: TestWith[WellcomeHttpApp, R]): R =
    withActorSystem { implicit actorSystem =>
      val httpMetrics = new HttpMetrics(
        name = metricsName,
        metrics = metrics
      )

      val sierraServiceConfig = SierraServiceConfig(
        baseUrl = f"$sierraApiUrl/iii/sierra-api",
        username = "username",
        password = "password"
      )

      val catalogueServiceConfig =
        CatalogueServiceConfig(s"$catalogueApiUrl/catalogue/v2")

      val stacksService: StacksService =
        new StacksServiceBuilder().buildT(
          StacksServiceConfig(
            catalogueServiceConfig,
            sierraServiceConfig
          )
        )

      val router: ItemsApi = new ItemsApi {
        override implicit val ec: ExecutionContext = global
        override implicit val stacksWorkService: StacksService = stacksService
      }

      val app = new WellcomeHttpApp(
        routes = router.routes,
        httpMetrics = httpMetrics,
        httpServerConfig = httpServerConfigTest,
        contextURL = contextURLTest,
        appName = metricsName
      )

      app.run()

      testWith(app)
    }

  def withApp[R](testWith: TestWith[WireMockServer, R]): R =
    withMockCatalogueServer { catalogueApiUrl: String =>
      withMockSierraServer {
        case (sierraApiUrl, server) =>
          val metrics = new MemoryMetrics()

          withApp(catalogueApiUrl, sierraApiUrl, metrics) { _ =>
            testWith(server)
          }
      }
    }
}
