package uk.ac.wellcome.platform.stacks.common.services.config.models

import akka.http.scaladsl.model.Uri

case class CatalogueServiceConfig(baseUrl: Option[String])

object CatalogueServiceConfig {
  def apply(baseUrl: String): CatalogueServiceConfig =
    CatalogueServiceConfig(Some(baseUrl))
}