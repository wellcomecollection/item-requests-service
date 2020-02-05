package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import akka.stream.ActorMaterializer
import uk.ac.wellcome.platform.stacks.common.models.{Identifier, StacksWorkIdentifier}

import scala.concurrent.Future

class AkkaCatalogueSource(
                        val baseUri: Uri = Uri(
                          "https://api.wellcomecollection.org/catalogue/v2"
                        )
                      )(
                        implicit
                        val system: ActorSystem,
                        val mat: ActorMaterializer
                      )
  extends CatalogueSource
  with AkkaClientServiceGet {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import CatalogueSource._

  // See https://developers.wellcomecollection.org/catalogue/v2/works/getwork
  def getWorkStub(id: StacksWorkIdentifier): Future[WorkStub] =
    get[WorkStub](
      path = Path(s"works/${id.value}"),
      params = Map(
        ("include", "items,identifiers")
      )
    )

  // See https://developers.wellcomecollection.org/catalogue/v2/works/getworks
  def getSearchStub(identifier: Identifier[_]): Future[SearchStub] =
    get[SearchStub](
      path = Path("works"),
      params = Map(
        ("include", "items,identifiers"),
        ("query", identifier.value.toString)
      )
    )
}
