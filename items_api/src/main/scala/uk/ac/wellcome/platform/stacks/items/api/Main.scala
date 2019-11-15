package uk.ac.wellcome.platform.stacks.items.api

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.monitoring.typesafe.MetricsBuilder
import uk.ac.wellcome.platform.catalogue
import uk.ac.wellcome.platform.sierra
import uk.ac.wellcome.platform.stacks.common.http.config.builders.HTTPServerBuilder
import uk.ac.wellcome.platform.stacks.common.http.{HttpMetrics, WellcomeHttpApp}
import uk.ac.wellcome.platform.stacks.common.services.{SierraService, StacksWorkService}
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

    val worksApiClient = new catalogue.ApiClient().setBasePath("http://localhost:8080/")
    val worksApi = new catalogue.api.WorksApi(worksApiClient)

    val sierraService = new SierraService(
      baseUrl = "http://localhost:8080/iii/sierra-api",
      username = "username",
      password = "password"
    )

    val workService = new StacksWorkService(worksApi, sierraService)

    val router: ItemsApi = new ItemsApi {
      override implicit val ec: ExecutionContext = ecMain
      override implicit val stacksWorkService: StacksWorkService = workService
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
