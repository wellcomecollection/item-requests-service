package uk.ac.wellcome.platform.stacks.common.sierra.config.builders

import com.typesafe.config.Config
import uk.ac.wellcome.platform.stacks.common.sierra.config.models.SierraApiConfig

import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._

object SierraApiConfigBuilder {

  def buildSierraApiConfig(config: Config): SierraApiConfig = {
    val user = config.required[String]("sierra.auth.user")
    val pass = config.required[String]("sierra.auth.pass")

    SierraApiConfig(
      user = user,
      pass = pass
    )
  }
}
