package uk.ac.wellcome.platform.requests.api.config.models

case class HTTPServerConfig(
  host: String,
  port: Int,
  externalBaseURL: String
)
