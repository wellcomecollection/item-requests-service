package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, HttpMethods, HttpRequest, HttpResponse, RequestEntity, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}

trait AkkaClientService {
  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  protected val baseUri: Uri

  protected def buildUri(path: String, params: Map[String, String] = Map.empty): Uri =
    baseUri
      .copy(path = baseUri.path + "/" + path)
      .withQuery(Query(params))
}

trait AkkaClientServiceGet extends AkkaClientService {
  protected def get[Out](path: String, params: Map[String, String] = Map.empty, headers: List[HttpHeader] = Nil)(
    implicit um: Unmarshaller[HttpResponse, Out]
  ): Future[Out] =
    for {
      response <- Http().singleRequest(
        HttpRequest(
          uri = buildUri(path, params),
          headers = headers
        )
      )
      t <- Unmarshal(response).to[Out]
    } yield t
}

trait AkkaClientServicePost extends AkkaClientService {
  protected def post[In, Out](path: String, body: Option[In] = None, params: Map[String, String] = Map.empty, headers: List[HttpHeader] = Nil)(
    implicit
      um: Unmarshaller[HttpResponse, Out],
      m: Marshaller[In, RequestEntity]
  ): Future[Option[Out]] =
    for {
      entity <- body match {
        case Some(body) => Marshal(body).to[RequestEntity]
        case None => Future.successful(HttpEntity.Empty)
      }

      response <- Http().singleRequest(
        HttpRequest(
          HttpMethods.POST,
          uri = buildUri(path, params),
          headers = headers,
          entity = entity
        )
      )

      result <- response.entity match {
        case e if e.isKnownEmpty() => Future.successful(None)
        case _ => Unmarshal(response).to[Out].map(Some(_))
      }
    } yield result
}

trait AkkaClientTokenExchange extends AkkaClientServicePost {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  case class AccessToken(access_token: String)

  val tokenPath: String

  protected def getToken(credentials: BasicHttpCredentials): Future[OAuth2BearerToken] = {
    for {
      token <- post[String, AccessToken](
        path = tokenPath,
        headers = List(Authorization(
          credentials
        ))
      )

      result <- token match {
        case Some(token) => Future.successful(
          OAuth2BearerToken(token.access_token)
        )
        case None => Future.failed(
          new Exception("No access token provided!")
        )
      }

    } yield result
  }
}
