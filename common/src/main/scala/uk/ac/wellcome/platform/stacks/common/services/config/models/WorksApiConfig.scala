package uk.ac.wellcome.platform.stacks.common.services.config.models

case class WorksApiConfig(baseUrl: Option[String])

object WorksApiConfig {
  def apply(baseUrl: String): WorksApiConfig =
    WorksApiConfig(Some(baseUrl))
}