package uk.ac.wellcome.platform.stacks.common.http

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, OAuth2BearerToken}

import java.time.Instant
import scala.concurrent.Future

trait TokenExchange {
  private val cachedToken: Option[OAuth2BearerToken] = None
  private val cachedTokenExpiryTime: Option[Instant] = None

  println(cachedToken)
  println(cachedTokenExpiryTime)

  protected def getNewToken(credentials: BasicHttpCredentials): Future[OAuth2BearerToken]

  protected def getToken(
    credentials: BasicHttpCredentials
  ): Future[OAuth2BearerToken] =
    getNewToken(credentials)
}