package uk.ac.wellcome.platform.stacks.items.api.fixtures

import java.net.URL

import org.scalatest.concurrent.ScalaFutures
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.monitoring.fixtures.MetricsSenderFixture
import uk.ac.wellcome.monitoring.memory.MemoryMetrics
import uk.ac.wellcome.platform.catalogue.ApiClient
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.stacks.common.http.fixtures.HttpFixtures
import uk.ac.wellcome.platform.stacks.common.http.{HttpMetrics, WellcomeHttpApp}
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
      catalogueApiBasePath: String,
      metrics: MemoryMetrics[Unit]
  )(testWith: TestWith[WellcomeHttpApp, R]): R =
    withActorSystem { implicit actorSystem =>
      withMaterializer(actorSystem) { implicit mat =>

        val httpMetrics = new HttpMetrics(
          name = metricsName,
          metrics = metrics
        )

        val apiClient = new ApiClient().setBasePath(s"$catalogueApiBasePath/catalogue/v2")

        val router: ItemsApi = new ItemsApi {
          override implicit val ec: ExecutionContext = global
          override implicit val worksApi: WorksApi = new WorksApi(apiClient)
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

  def withConfiguredApp[R](catalogueApiBasePath: String)(
      testWith: TestWith[(MemoryMetrics[Unit], String), R]
  ): R = {
    val metrics = new MemoryMetrics[Unit]()

    withApp(catalogueApiBasePath, metrics) { _ =>
      testWith((metrics, httpServerConfigTest.externalBaseURL))
    }
  }
}
