package uk.ac.wellcome.platform.status.api

import io.circe.parser.parse
import io.circe.optics.JsonPath._
import scalaj.http.Http
import uk.ac.wellcome.json.JsonUtil._

trait CatalogueApi

// TODO: Use eithers not options
object HttpCatalogueApi extends CatalogueApi {
  private case class IdentifierType(id: String, label: String, `type`: String)
  private case class Identifier(identifierType: IdentifierType, value: String, `type`: String)
  private case class Item(id: Option[String], identifiers: List[Identifier])

  val rootUrl = "https://api.wellcomecollection.org/catalogue/v2/works"

  def getItemINumber(id: String) = {
    val jsonString = Http(s"$rootUrl?include=items,identifiers&query=$id")
      .header("Accept", "application/json")
      .asString
      .body

    (parse(jsonString) map { json =>
      val items =
        root.results(0).items.each.as[Item].getAll(json)

      val identifier = items.find(_.id.contains(id)) flatMap (item => item.identifiers.find(id => id.identifierType.id == "sierra-identifier" ))
      identifier map (_.value)
    } toOption).flatten
  }
}
