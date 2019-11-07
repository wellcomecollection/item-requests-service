package uk.ac.wellcome.platform.stacks.common.sierra.config.builders

import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.sierra.config.models.SierraApiConfig

import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

object SierraApiConfigBuilder {

  def buildSierraApiConfig(config: Config): SierraApiConfig = {
    val key = config.required[String]("sierra.api.key")
    val secret = config.required[String]("sierra.api.secret")

    SierraApiConfig(
      key = key,
      secret = secret
    )
  }
}
