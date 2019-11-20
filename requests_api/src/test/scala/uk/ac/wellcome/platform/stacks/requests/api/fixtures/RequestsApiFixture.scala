package uk.ac.wellcome.platform.stacks.requests.api.fixtures

import java.net.URL

import org.scalatest.concurrent.ScalaFutures
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.monitoring.fixtures.MetricsSenderFixture
import uk.ac.wellcome.monitoring.memory.MemoryMetrics
import uk.ac.wellcome.platform.stacks.common.http.fixtures.HttpFixtures
import uk.ac.wellcome.platform.stacks.common.http.{HttpMetrics, WellcomeHttpApp}
import uk.ac.wellcome.platform.stacks.common.services.StacksWorkService
import uk.ac.wellcome.platform.stacks.common.services.config.builders.{SierraServiceBuilder, WorksApiBuilder}
import uk.ac.wellcome.platform.stacks.common.services.config.models.{SierraServiceConfig, WorksApiConfig}
import uk.ac.wellcome.platform.stacks.requests.api.RequestsApi

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait RequestsApiFixture
  extends ScalaFutures
    with HttpFixtures
    with MetricsSenderFixture {

  val metricsName = "RequestsApiFixture"

  val contextURLTest = new URL(
    "http://api.wellcomecollection.org/requests/v1/context.json"
  )

  private def withApp[R](
                          catalogueApiUrl: String,
                          sierraApiUrl: String,
                          metrics: MemoryMetrics[Unit]
                        )(testWith: TestWith[WellcomeHttpApp, R]): R =
    withActorSystem { implicit actorSystem =>
      withMaterializer(actorSystem) { implicit materializer =>
        val httpMetrics = new HttpMetrics(
          name = metricsName,
          metrics = metrics
        )

        val sierraServiceConfig = SierraServiceConfig(
          baseUrl = Some(f"$sierraApiUrl/iii/sierra-api"),
          username = "username",
          password = "password"
        )

        val worksApiConfig = WorksApiConfig(s"$catalogueApiUrl/catalogue/v2")

        val sierraServiceBuilder = new SierraServiceBuilder()
        val sierraService = sierraServiceBuilder.buildT(sierraServiceConfig)

        val worksApi = WorksApiBuilder.buildT(worksApiConfig)
        val workService = new StacksWorkService(worksApi, sierraService)

        val router: RequestsApi = new RequestsApi {
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
