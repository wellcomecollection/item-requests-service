package uk.ac.wellcome.platform.requests.api

import io.circe.Decoder
import scalaj.http.Http
import uk.ac.wellcome.json.JsonUtil._

case class SierraToken(accessToken: String, tokenType: String, expiresIn: Int)
object SierraToken {
  implicit val decodeUser: Decoder[SierraToken] =
    Decoder.forProduct3("access_token", "token_type", "expires_in")(
      SierraToken.apply)
}
case class SierraItem(id: String,
                      location: SierraLocation,
                      status: SierraStatus,
                      barcode: String,
                      callNumber: String)
case class SierraLocation(code: String, name: String)
case class SierraStatus(code: String, display: String)
case class SierraPatron(id: Int,
                        names: List[String] = Nil,
                        emails: List[String] = Nil)
case class SierraPatronHolds(total: Int, entries: List[SierraPatronHoldEntry])
case class SierraHoldStatus(code: String, name: String)
case class SierraPatronHoldEntry(id: String,
                                 record: String,
                                 patron: String,
                                 pickupLocation: SierraLocation,
                                 status: SierraHoldStatus)
case class SierraPatronHoldRequest(recordType: String,
                                   recordNumber: Int,
                                   pickupLocation: String,
                                   note: String)

trait SierrApi {
  val authUser: String
  val authPass: String
  val rootUrl: String
}

// TODO: Use eithers not options
class SierraHttpApi(val authUser: String, val authPass: String)
    extends SierrApi {

  val rootUrl = "https://libsys.wellcomelibrary.org/iii/sierra-api/v3"

  private def getToken(): Option[SierraToken] = {
    val resp =
      Http(s"$rootUrl/token")
        .postData("grant_type=client_credentials")
        .auth(authUser, authPass)
        .asString

    fromJson[SierraToken](resp.body).toOption
  }

  private def authed(path: String) = getToken map { token =>
    Http(s"$rootUrl$path")
      .header("Authorization", s"Bearer ${token.accessToken}")
      .header("Accept", "application/json")
  }

  private def get[T](path: String)(implicit decoder: Decoder[T]) = {
    authed(path) flatMap { req =>
      fromJson[T](req.asString.body).toOption
    }
  }

  def getItem(id: String) = {
    get[SierraItem](s"/items/$id")
  }

  def getPatron(id: Int) = {
    get[SierraPatron](s"/patrons/$id?fields=names,emails")
  }

  def getPatronHolds(id: Int) = {
    get[SierraPatronHolds](s"/patrons/$id/holds")
  }

  // TODO: Return type
  def deletePatronHold(holdId: Int) = {
    authed(s"/patrons/holds/$holdId") flatMap { req =>
      req.method("DELETE").asString
      None
    }
  }

  def postPatronPlaceHold(patronId: Int, itemId: Int) = {
    val holdRequest = SierraPatronHoldRequest(
      "i",
      recordNumber = itemId,
      pickupLocation = "-",
      note =
        "THIS IS A TEST AND SHOULD BE IGNORED. IT WILL BE DELETED AUTOMATICALLY. IF THIS IS CAUSING HASSLES PLEASE EMAIL j.gorrie@wellcome.ac.uk."
    )
    authed(s"/patrons/$patronId/holds/requests") flatMap { req =>
      val resp = req
        .header("content-type", "application/json")
        .postData(toJson(holdRequest).getOrElse(""))
        .asString

      resp code match {
        case 204 => Some(holdRequest)
        case _   => None
      }
    }
  }

}
