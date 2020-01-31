package uk.ac.wellcome.platform.stacks.common.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}

trait AkkaClientServiceWrappper {
  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  implicit val ec: ExecutionContextExecutor = system.dispatcher

  protected val defaultBaseUri: Uri
  protected val maybeBaseUri: Option[Uri]

  protected def query[T](path: String, params: String)(
    implicit um: Unmarshaller[HttpResponse, T]
  ): Future[T] = {
    val baseUri = maybeBaseUri.getOrElse(defaultBaseUri)
    val uri = s"${baseUri}/${path}?${params}"

    for {
      response <- Http().singleRequest(HttpRequest(uri = uri))
      t <- Unmarshal(response).to[T]
    } yield t
  }
}
