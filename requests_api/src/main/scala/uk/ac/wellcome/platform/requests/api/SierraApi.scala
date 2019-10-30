package uk.ac.wellcome.platform.requests.api

import io.circe.Decoder
import scalaj.http.Http
import uk.ac.wellcome.json.JsonUtil._
import uk.ac.wellcome.platform.requests.api.config.models.SierraApiConfig

case class SierraToken(accessToken: String, tokenType: String, expiresIn: Int)
object SierraToken {
  implicit val decodeSierraToken: Decoder[SierraToken] =
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
case class SierraPatron(id: Int)
case class SierraPatronHolds(total: Int, entries: List[SierraPatronHoldEntry])
case class SierraHoldStatus(code: String, name: String)
case class SierraPatronHoldEntry(id: String,
                                 record: String,
                                 patron: String,
                                 pickupLocation: SierraLocation,
                                 status: SierraHoldStatus)
case class SierraPatronHoldRequest(recordType: String,
                                   recordNumber: String,
                                   pickupLocation: String,
                                   note: String)
case object DeleteSuccess

trait SierraApi {
  val config: SierraApiConfig
  val rootUrl: String
}

// TODO: Use eithers not options
class HttpSierraApi(val config: SierraApiConfig)
    extends SierraApi {

  val rootUrl = "https://libsys.wellcomelibrary.org/iii/sierra-api/v5"

  private def getToken(): Option[SierraToken] = {
    val resp =
      Http(s"$rootUrl/token")
        .postData("grant_type=client_credentials")
        .auth(config.user, config.pass)
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

  def validatePatron(patronId: String, pass: String) = {
    authed(s"/patrons/validate") map { req =>
      req
        .header("content-type", "application/json")
        .postData(s"""{ "barcode": "$patronId", "pin": "$pass" }""")
        .asString
    }
  }

  def getPatron(id: String) = {
    get[SierraPatron](s"/patrons/$id")
  }


  def getPatronHolds(patronId: String) = {
    get[SierraPatronHolds](s"/patrons/$patronId/holds")
  }

  def deletePatronHolds(patronId: String) = {
    authed(s"/patrons/$patronId/holds") map { req =>
      req.method("DELETE").asString
      DeleteSuccess
    }
  }

  def postPatronPlaceHold(patronId: String, itemId: String) = {
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
        case _   => Some(holdRequest.copy(note = s"Error ${resp.body}"))
      }
    }
  }
}
