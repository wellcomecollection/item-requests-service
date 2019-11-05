package uk.ac.wellcome.platform.stacks.items.api.fixtures

import java.net.URL

import org.scalatest.concurrent.ScalaFutures
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.monitoring.fixtures.MetricsSenderFixture
import uk.ac.wellcome.monitoring.memory.MemoryMetrics
import uk.ac.wellcome.platform.stacks.common.sierra.config.models.SierraApiConfig
import uk.ac.wellcome.platform.stacks.common.sierra.http.{HttpMetrics, WellcomeHttpApp}
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi
import uk.ac.wellcome.platform.stacks.items.api.ItemsApi

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait ItemsApiFixture
    extends ScalaFutures
    with HttpFixtures
    with MetricsSenderFixture {

  val metricsName = "StatusApiFixture"

  val contextURLTest = new URL(
    "http://api.wellcomecollection.org/requests/v1/context.json"
  )

  private def withApp[R](
      metrics: MemoryMetrics[Unit]
  )(testWith: TestWith[WellcomeHttpApp, R]): R =
    withActorSystem { implicit actorSystem =>
      withMaterializer(actorSystem) { implicit materializer =>
        val httpMetrics = new HttpMetrics(
          name = metricsName,
          metrics = metrics
        )

        val router: ItemsApi = new ItemsApi {
          override implicit val ec: ExecutionContext = global
          override implicit val sierraApi: SierraApi = new SierraApi(SierraApiConfig(
            "hello",
            "123"
          ))
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

  def withConfiguredApp[R]()(
      testWith: TestWith[(MemoryMetrics[Unit], String), R]
  ): R = {
    val metrics = new MemoryMetrics[Unit]()

    withApp(metrics) { _ =>
      testWith((metrics, httpServerConfigTest.externalBaseURL))
    }
  }
}
