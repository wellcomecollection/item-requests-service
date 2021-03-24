package uk.ac.wellcome.platform.stacks.common.services.config.builders

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.config.TypesafeBuilder
import uk.ac.wellcome.platform.stacks.common.services.CatalogueService
import uk.ac.wellcome.platform.stacks.common.services.config.models.CatalogueServiceConfig
import uk.ac.wellcome.platform.stacks.common.services.source.AkkaCatalogueSource
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

class CatalogueServiceBuilder()(
  implicit
  val system: ActorSystem
) extends TypesafeBuilder[CatalogueService, CatalogueServiceConfig] {

  implicit val ec = system.dispatcher

  def buildConfig(config: Config): CatalogueServiceConfig =
    CatalogueServiceConfig(config.requireString("catalogue.api.baseUrl"))

  def buildT(config: CatalogueServiceConfig): CatalogueService =
    new CatalogueService(
      new AkkaCatalogueSource(Uri(config.baseUrl))
    )
}
