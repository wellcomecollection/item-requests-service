package uk.ac.wellcome.platform.stacks.common.config

import com.typesafe.config.Config
import grizzled.slf4j.Logging

trait TypesafeBuilder[T, Conf] extends Logging {
  def buildConfig(config: Config): Conf
  def buildT(wellcomeConfig: Conf): T

  def build(config: Config) = {
    val conf = buildConfig(config)
    debug(f"Configuring with: $conf")

    buildT(conf)
  }
}
