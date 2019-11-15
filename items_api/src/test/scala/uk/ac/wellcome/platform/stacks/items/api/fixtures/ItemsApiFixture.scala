package uk.ac.wellcome.platform.stacks.items.api.fixtures

import java.net.URL

import org.scalatest.concurrent.ScalaFutures
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.monitoring.fixtures.MetricsSenderFixture
import uk.ac.wellcome.monitoring.memory.MemoryMetrics
import uk.ac.wellcome.platform.sierra
import uk.ac.wellcome.platform.catalogue
import uk.ac.wellcome.platform.stacks.common.http.fixtures.HttpFixtures
import uk.ac.wellcome.platform.stacks.common.http.{HttpMetrics, WellcomeHttpApp}
import uk.ac.wellcome.platform.stacks.common.services.{SierraService, StacksWorkService}
import uk.ac.wellcome.platform.stacks.items.api.ItemsApi

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait ItemsApiFixture
    extends ScalaFutures
    with HttpFixtures
    with MetricsSenderFixture {

  val metricsName = "ItemsApiFixture"

  val contextURLTest = new URL(
    "http://api.wellcomecollection.org/stacks/v1/context.json"
  )

  private def withApp[R](
                          catalogueApiUrl: String,
                          sierraApiUrl: String,
      metrics: MemoryMetrics[Unit]
  )(testWith: TestWith[WellcomeHttpApp, R]): R =
    withActorSystem { implicit actorSystem =>
      withMaterializer(actorSystem) { implicit mat =>

        val httpMetrics = new HttpMetrics(
          name = metricsName,
          metrics = metrics
        )

        val sierraService = new SierraService(
          baseUrl = f"$sierraApiUrl/iii/sierra-api",
          username = "username",
          password = "password"
        )

        val apiClient = new catalogue.ApiClient().setBasePath(s"$catalogueApiUrl/catalogue/v2")
        val worksApi = new catalogue.api.WorksApi(apiClient)

        val workService = new StacksWorkService(worksApi, sierraService)

        val router: ItemsApi = new ItemsApi {
          override implicit val ec: ExecutionContext = global
          override implicit val stacksWorkService: StacksWorkService = workService
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
    }

  def withConfiguredApp[R](
                            catalogueApiUrl: String,
                            sierraApiUrl: String
                          )(
      testWith: TestWith[(MemoryMetrics[Unit], String), R]
  ): R = {
    val metrics = new MemoryMetrics[Unit]()

    withApp(catalogueApiUrl, sierraApiUrl, metrics) { _ =>
      testWith((metrics, httpServerConfigTest.externalBaseURL))
    }
  }
}
