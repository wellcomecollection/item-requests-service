package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import io.circe.Json
import uk.ac.wellcome.platform.sierra
import uk.ac.wellcome.platform.sierra.ApiClient
import uk.ac.wellcome.platform.sierra.api.V5itemsApi

import scala.concurrent.{ExecutionContext, Future}


class SierraService(baseUrl: String, username: String, password: String)(
  implicit
    as: ActorSystem,
    am: ActorMaterializer,
    ec: ExecutionContext
) {

  object AuthToken {
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    val tokenPath = "v5/token"
    val authHeader = Authorization(BasicHttpCredentials(username, password))

    val tokenRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = f"$baseUrl/$tokenPath",
      headers = List(authHeader),
    )

    def get(): Future[String] = for {
      response <- Http().singleRequest(tokenRequest)
      jsonDoc <- Unmarshal(response).to[Json]
      accessToken <- jsonDoc.hcursor.downField("access_token").as[String] match {
        case Left(e) => Future.failed(e)
        case Right(token) => Future.successful(token)
      }
    } yield accessToken
  }

  def itemsApi(): Future[V5itemsApi] = AuthToken.get().map { token =>
    val sierraApiClient = new sierra.ApiClient()

    sierraApiClient.setBasePath(baseUrl)
    sierraApiClient.setAccessToken(token)

    new sierra.api.V5itemsApi(sierraApiClient)
  }
}
