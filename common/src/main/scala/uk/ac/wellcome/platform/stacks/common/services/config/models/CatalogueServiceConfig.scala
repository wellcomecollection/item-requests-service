package uk.ac.wellcome.platform.stacks.common.services.config.models

import akka.http.scaladsl.model.Uri

case class CatalogueServiceConfig(baseUrl: Option[Uri])

object CatalogueServiceConfig {
  def apply(baseUrl: Uri): CatalogueServiceConfig =
    CatalogueServiceConfig(Some(baseUrl))
}