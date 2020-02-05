package uk.ac.wellcome.platform.stacks.common.services.config.builders

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.config.TypesafeBuilder
import uk.ac.wellcome.platform.stacks.common.services.CatalogueService
import uk.ac.wellcome.platform.stacks.common.services.config.models.CatalogueServiceConfig
import uk.ac.wellcome.platform.stacks.common.services.source.AkkaCatalogueSource
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

class CatalogueServiceBuilder()(
    implicit
      val system: ActorSystem,
      val mat: ActorMaterializer
) extends TypesafeBuilder[CatalogueService, CatalogueServiceConfig] {

  implicit val ec = system.dispatcher

  def buildConfig(config: Config): CatalogueServiceConfig =
    CatalogueServiceConfig(config.required("catalogue.api.baseUrl"))

  def buildT(config: CatalogueServiceConfig): CatalogueService =
    new CatalogueService(
      new AkkaCatalogueSource(Uri(config.baseUrl))
    )
}
