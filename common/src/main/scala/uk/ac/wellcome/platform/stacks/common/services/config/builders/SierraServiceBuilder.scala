package uk.ac.wellcome.platform.stacks.common.services.config.builders

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.config.TypesafeBuilder
import uk.ac.wellcome.platform.stacks.common.services.SierraService
import uk.ac.wellcome.platform.stacks.common.services.config.models.SierraServiceConfig
import uk.ac.wellcome.platform.stacks.common.services.source.AkkaSierraSource
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

import scala.concurrent.ExecutionContext

class SierraServiceBuilder()(
  implicit am: ActorMaterializer,
  as: ActorSystem,
  ec: ExecutionContext
) extends TypesafeBuilder[SierraService, SierraServiceConfig] {

  def buildConfig(config: Config): SierraServiceConfig = {
    val username = config.requireString("sierra.api.key")
    val password = config.requireString(("sierra.api.secret"))
    val baseUrl = config.requireString("sierra.api.baseUrl")

    SierraServiceConfig(baseUrl, username, password)
  }

  def buildT(config: SierraServiceConfig): SierraService = new SierraService(
    new AkkaSierraSource(
      baseUri = Uri(config.baseUrl),
      credentials = BasicHttpCredentials(
        username = config.username,
        password = config.password
      )
    )
  )
}
