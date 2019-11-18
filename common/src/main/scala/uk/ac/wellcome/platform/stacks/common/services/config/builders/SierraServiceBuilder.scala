package uk.ac.wellcome.platform.stacks.common.services.config.builders

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.config.TypesafeBuilder
import uk.ac.wellcome.platform.stacks.common.services.SierraService
import uk.ac.wellcome.platform.stacks.common.services.config.models.SierraServiceConfig
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

import scala.concurrent.ExecutionContext

class SierraServiceBuilder()(
  implicit am: ActorMaterializer, as: ActorSystem, ec: ExecutionContext
) extends TypesafeBuilder[SierraService, SierraServiceConfig] {

  def buildConfig(config: Config): SierraServiceConfig = {
    val username = config.required[String]("sierra.api.key")
    val password = config.required[String]("sierra.api.secret")
    val baseUrl = config.get[String]("sierra.api.baseUrl")

    SierraServiceConfig(baseUrl, username, password)
  }

  def buildT(config: SierraServiceConfig): SierraService = new SierraService(
      baseUrl = config.baseUrl,
      username = config.username,
      password = config.password
    )
}

