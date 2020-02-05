package uk.ac.wellcome.platform.stacks.common.services.config.builders

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.config.TypesafeBuilder
import uk.ac.wellcome.platform.stacks.common.services.StacksService
import uk.ac.wellcome.platform.stacks.common.services.config.models.StacksServiceConfig

import scala.concurrent.ExecutionContext

class StacksServiceBuilder()(
    implicit am: ActorMaterializer,
    as: ActorSystem,
    ec: ExecutionContext
) extends TypesafeBuilder[StacksService, StacksServiceConfig] {

  override def buildConfig(config: Config): StacksServiceConfig =
    StacksServiceConfig(
      new CatalogueServiceBuilder().buildConfig(config),
      new SierraServiceBuilder().buildConfig(config)
    )

  override def buildT(wellcomeConfig: StacksServiceConfig): StacksService = {
    new StacksService(
      new CatalogueServiceBuilder()
        .buildT(wellcomeConfig.catalogueServiceConfig),
      new SierraServiceBuilder()
        .buildT(wellcomeConfig.sierraServiceConfig)
    )
  }
}
