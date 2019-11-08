package uk.ac.wellcome.platform.stacks.items.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging
import uk.ac.wellcome.platform.catalogue.ApiClient
import uk.ac.wellcome.platform.catalogue.api.WorksApi
import uk.ac.wellcome.platform.catalogue.models.Work
import uk.ac.wellcome.platform.stacks.common.sierra.services.SierraApi

import scala.concurrent.ExecutionContext

case class TestResponse(workId: String, itemId: String)

trait ItemsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext
  implicit val sierraApi: SierraApi

  val routes: Route = concat(
    pathPrefix("works") {
      path(Segment) { workId: String =>
        get {
          val apiClient = new ApiClient().setBasePath("http://localhost:8080/catalogue/v2/")
          val worksApi = new WorksApi(apiClient)

          val work: Work = worksApi.getWork(workId, "items")

          val items = work.getItems()

          println(items)

          complete("ok")
        }
      }
    }
  )
}
