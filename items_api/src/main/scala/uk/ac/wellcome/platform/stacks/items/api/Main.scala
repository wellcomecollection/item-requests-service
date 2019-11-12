package uk.ac.wellcome.platform.stacks.items.api

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.monitoring.typesafe.MetricsBuilder
import uk.ac.wellcome.platform.catalogue.ApiClient
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.stacks.common.http.config.builders.HTTPServerBuilder
import uk.ac.wellcome.platform.stacks.common.http.{HttpMetrics, WellcomeHttpApp}
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

    // TODO: Set from config
    val apiClient = new ApiClient().setBasePath("http://localhost:8080/")

    val router: ItemsApi = new ItemsApi {
      override implicit val ec: ExecutionContext = ecMain
      override implicit val worksApi: WorksApi = new WorksApi(apiClient)
    }



    val appName = "ItemsApi"

    new WellcomeHttpApp(
      routes = router.routes,
      httpMetrics = new HttpMetrics(
        name = appName,
        metrics = MetricsBuilder.buildMetricsSender(config)
      ),
      httpServerConfig = HTTPServerBuilder.buildHTTPServerConfig(config),
      contextURL = new URL(
        "https://api.wellcomecollection.org/stacks/v1/context.json"),
      appName = appName
    )
  }
}
