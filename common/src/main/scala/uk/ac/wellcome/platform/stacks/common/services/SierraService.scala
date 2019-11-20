package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import io.circe.Json
import uk.ac.wellcome.platform.sierra
import uk.ac.wellcome.platform.sierra.api.{V5itemsApi, V5patronsApi}

import scala.concurrent.{ExecutionContext, Future}


class SierraService(baseUrl: Option[String], username: String, password: String)(
  implicit
    as: ActorSystem,
    am: ActorMaterializer,
    ec: ExecutionContext
) {
  val sierraApiClient = new sierra.ApiClient()

  baseUrl.foreach { sierraApiClient.setBasePath }
  val tokenUrlBase = baseUrl.getOrElse(sierraApiClient.getBasePath)

  object AuthToken {
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

    val tokenPath = "v5/token"
    val authHeader = Authorization(BasicHttpCredentials(username, password))

    val tokenRequest = HttpRequest(
      method = HttpMethods.POST,
      uri = f"$tokenUrlBase/$tokenPath",
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
    sierraApiClient.setAccessToken(token)

    new sierra.api.V5itemsApi(sierraApiClient)
  }

  def patronsApi(): Future[V5patronsApi] = AuthToken.get().map { token =>
    sierraApiClient.setAccessToken(token)

    new sierra.api.V5patronsApi(sierraApiClient)
  }
}
