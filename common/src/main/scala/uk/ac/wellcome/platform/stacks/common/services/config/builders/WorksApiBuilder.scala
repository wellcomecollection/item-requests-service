package uk.ac.wellcome.platform.stacks.common.services.config.builders

import com.typesafe.config.Config
import uk.ac.wellcome.platform.catalogue
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.stacks.common.config.TypesafeBuilder
import uk.ac.wellcome.platform.stacks.common.services.config.models.WorksApiConfig
import uk.ac.wellcome.typesafe.config.builders.EnrichConfig._


object WorksApiBuilder extends TypesafeBuilder[WorksApi, WorksApiConfig]{
  def buildConfig(config: Config): WorksApiConfig = {
    val maybeBaseUrl: Option[String] =
      config.get("catalogue.api.baseUrl")

    WorksApiConfig(maybeBaseUrl)
  }

  def buildT(config: WorksApiConfig): WorksApi = {
    val apiClient = new catalogue.ApiClient()

    config.baseUrl.foreach { apiClient.setBasePath }

    new catalogue.api.WorksApi(apiClient)
  }
}
