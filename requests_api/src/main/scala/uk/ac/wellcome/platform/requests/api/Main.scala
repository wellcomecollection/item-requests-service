package uk.ac.wellcome.platform.requests.api

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.monitoring.typesafe.MetricsBuilder
import uk.ac.wellcome.platform.stacks.common.sierra.config.builders.{HTTPServerBuilder, SierraApiConfigBuilder}
import uk.ac.wellcome.platform.stacks.common.sierra.http.{HttpMetrics, WellcomeHttpApp}
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi
import uk.ac.wellcome.typesafe.WellcomeTypesafeApp
import uk.ac.wellcome.typesafe.config.builders.AkkaBuilder

import scala.concurrent.ExecutionContext

object Main extends WellcomeTypesafeApp {
  runWithConfig { config: Config =>
    implicit val asMain: ActorSystem =
      AkkaBuilder.buildActorSystem()

    implicit val ecMain: ExecutionContext =
      AkkaBuilder.buildExecutionContext()

    implicit val amMain: ActorMaterializer =
      AkkaBuilder.buildActorMaterializer()

    val sierraApiConfig = SierraApiConfigBuilder.buildSierraApiConfig(config)

    val router: RequestsApi = new RequestsApi {
      override implicit val ec: ExecutionContext = ecMain
      override implicit val sierraApi: SierraApi = new SierraApi(sierraApiConfig)
    }

    val appName = "RequestsApi"

    new WellcomeHttpApp(
      routes = router.routes,
      httpMetrics = new HttpMetrics(
        name = appName,
        metrics = MetricsBuilder.buildMetricsSender(config)
      ),
      httpServerConfig = HTTPServerBuilder.buildHTTPServerConfig(config),
      contextURL = new URL(
        "https://api.wellcomecollection.org/item-requests/v1/context.json"),
      appName = appName
    )
  }
}
