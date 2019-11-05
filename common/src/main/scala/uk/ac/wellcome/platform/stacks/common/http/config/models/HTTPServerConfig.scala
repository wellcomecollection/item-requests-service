package uk.ac.wellcome.platform.stacks.common.sierra.config.models

case class HTTPServerConfig(
  host: String,
  port: Int,
  externalBaseURL: String
)
