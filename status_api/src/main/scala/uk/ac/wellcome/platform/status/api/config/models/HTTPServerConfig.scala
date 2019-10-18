package uk.ac.wellcome.platform.status.api.config.models

case class HTTPServerConfig(
  host: String,
  port: Int,
  externalBaseURL: String
)
