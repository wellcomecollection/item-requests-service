package uk.ac.wellcome.platform.requests.api

import akka.http.scaladsl.server.Route
import grizzled.slf4j.Logging
import scala.concurrent.ExecutionContext
import uk.ac.wellcome.json.JsonUtil._

case class TestResponse(workId: String, itemId: String)
trait RequestsApi extends Logging {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  implicit val ec: ExecutionContext

  val routes: Route = path("works" / Segment / "items" / Segment) {
    case (workId, itemId) =>
      complete(TestResponse(workId, itemId))
  }
}
