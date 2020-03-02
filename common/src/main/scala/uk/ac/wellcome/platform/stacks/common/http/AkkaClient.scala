package uk.ac.wellcome.platform.stacks.common.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model.Uri.{Path, Query}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}

trait AkkaClient {

  sealed trait Response[T] {
    val content: Option[T]
  }
  case class SuccessResponse[T](content: Option[T]) extends Response[T]
  case class FailureResponse[T](content: Option[T]) extends Response[T]

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  protected val baseUri: Uri

  import Path._

  protected def buildUri(
    path: Path,
    params: Map[String, String] = Map.empty
  ): Uri =
    baseUri
      .withPath(baseUri.path ++ Slash(path))
      .withQuery(Query(params))
}

trait AkkaClientGet extends AkkaClient {
  protected def get[Out](
    path: Path,
    params: Map[String, String] = Map.empty,
    headers: List[HttpHeader] = Nil
  )(
    implicit um: Unmarshaller[HttpResponse, Out]
  ): Future[Response[Out]] =
    for {
      response <- Http().singleRequest(
        HttpRequest(
          uri = buildUri(path, params),
          headers = headers
        )
      )

      result <- response.entity match {
        case e if e.isKnownEmpty() => Future.successful(None)
        case _                     => Unmarshal(response).to[Out].map(Some(_))
      }

    } yield response.status match {
        case r if r.isSuccess() => SuccessResponse(result)
        case _ => FailureResponse(result)
    }
}

trait AkkaClientPost extends AkkaClient {
  protected def post[In, Out](
    path: Path,
    body: Option[In] = None,
    params: Map[String, String] = Map.empty,
    headers: List[HttpHeader] = Nil
  )(
    implicit
    um: Unmarshaller[HttpResponse, Out],
    m: Marshaller[In, RequestEntity]
  ): Future[Response[Out]] =
    for {
      entity <- body match {
        case Some(body) => Marshal(body).to[RequestEntity]
        case None       => Future.successful(HttpEntity.Empty)
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
        case _                     => Unmarshal(response).to[Out].map(Some(_))
      }

    } yield response.status match {
    case r if r.isSuccess() => SuccessResponse(result)
    case _ => FailureResponse(result)
  }
}

trait AkkaClientTokenExchange extends AkkaClientPost {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  case class AccessToken(access_token: String)

  val tokenPath: Path

  protected def getToken(
    credentials: BasicHttpCredentials
  ): Future[OAuth2BearerToken] = {
    for {
      response <- post[String, AccessToken](
        path = tokenPath,
        headers = List(
          Authorization(
            credentials
          )
        )
      )

      result <- response match {
        case SuccessResponse(Some(token)) =>
          Future.successful(
            OAuth2BearerToken(token.access_token)
          )
        case _ =>
          Future.failed(
            new Exception(s"Failed to get access token.")
          )
      }

    } yield result
  }
}
