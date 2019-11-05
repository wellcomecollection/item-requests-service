package uk.ac.wellcome.platform.stacks.common.sierra.services

import io.circe.Decoder
import scalaj.http.Http
import uk.ac.wellcome.json.JsonUtil._
import uk.ac.wellcome.platform.stacks.common.sierra.config.models.SierraApiConfig
import uk.ac.wellcome.platform.stacks.common.sierra.models.{SierraItem, SierraPatron, SierraPatronHolds, SierraToken}



class SierraApi(val config: SierraApiConfig) {
  case object DeleteSuccess

  val rootUrl = "https://libsys.wellcomelibrary.org/iii/sierra-api/v5"

  private def getToken: Option[SierraToken] = {
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

  def getItem(id: String): Option[SierraItem] = {
    get[SierraItem](s"/items/$id")
  }

  def validatePatron(patronId: String, pass: String): Option[Unit] = {
    authed(s"/patrons/validate") map { req =>
      val resp = req
        .header("content-type", "application/json")
        .postData(s"""{ "barcode": "$patronId", "pin": "$pass" }""")
        .asString

      println(resp.body)
    }

  }

  def getPatron(id: String): Option[SierraPatron] = {
    get[SierraPatron](s"/patrons/$id?fields=names,emails")
  }


  def getPatronHolds(patronId: String): Option[SierraPatronHolds] = {
    get[SierraPatronHolds](s"/patrons/$patronId/holds")
  }

  def deletePatronHolds(patronId: String): Option[DeleteSuccess.type] = {
    authed(s"/patrons/$patronId/holds") map { req =>
      req.method("DELETE").asString
      DeleteSuccess
    }
  }

  def postPatronPlaceHold(patronId: String, itemId: String): Option[SierraPatronHolds.Request] = {
    val holdRequest = SierraPatronHolds.Request(
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
