package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._
import io.circe.Json
import uk.ac.wellcome.platform.sierra
import uk.ac.wellcome.platform.sierra.api.{V5itemsApi, V5patronsApi}
import uk.ac.wellcome.platform.sierra.models.{Hold, HoldResultSet, PatronHoldPost}
import uk.ac.wellcome.platform.stacks.common.models._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class SierraService(baseUrl: Option[String], username: String, password: String)(
  implicit
  as: ActorSystem,
  am: ActorMaterializer,
  ec: ExecutionContext
) {
  val sierraApiClient = new sierra.ApiClient()

  baseUrl.foreach {
    sierraApiClient.setBasePath
  }

  private val tokenUrlBase =
    baseUrl.getOrElse(sierraApiClient.getBasePath)

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

  protected def itemsApi(): Future[V5itemsApi] = AuthToken.get().map { token =>
    sierraApiClient.setAccessToken(token)

    new sierra.api.V5itemsApi(sierraApiClient)
  }

  protected def patronsApi(): Future[V5patronsApi] = AuthToken.get().map { token =>
    sierraApiClient.setAccessToken(token)

    new sierra.api.V5patronsApi(sierraApiClient)
  }

  protected def getHoldResultSet(userIdentity: StacksUserIdentifier): Future[HoldResultSet] = for {
    patronsApi <- patronsApi()
    holdResultSet = patronsApi.getTheHoldsDataForASinglePatronRecord(
      userIdentity.value, 100, 0,
      List.empty[String].asJava
    )
  } yield holdResultSet

  protected def getStacksPickupFromHold(hold: Hold): StacksPickup = {
    StacksPickup(
      location = StacksLocation(
        hold.getPickupLocation.getCode,
        hold.getPickupLocation.getName
      ),
      // This should be a simpler conversion to a Java Instant
      pickUpBy = Instant.ofEpochSecond(
        hold.getPickupByDate.toEpochSecond
      )
    )
  }

  protected def getSierraItemIdentifierFrom(hold: Hold): Future[SierraItemIdentifier] = Future {
    hold.getRecordType match {
      case "i" => SierraItemIdentifier(hold)
      case _ => throw
        new Throwable(
          f"Could not get SierraItemIdentifier from hold! ($hold)"
        )

    }
  }

  protected def getStacksHoldStatusFrom(hold: Hold): StacksHoldStatus =
    StacksHoldStatus(
      id = hold.getStatus.getCode,
      label = hold.getStatus.getName
    )

  def getStacksUserHolds(userId: StacksUserIdentifier): Future[StacksUserHolds[SierraItemIdentifier]] = for {
    holdResultSet <- getHoldResultSet(userId)
    entries = holdResultSet.getEntries.asScala.toList

    sierraItemIdentifiers <- entries.traverse(getSierraItemIdentifierFrom)
    holdStatuses = entries.map(getStacksHoldStatusFrom)
    stacksPickups = entries.map(getStacksPickupFromHold)

    userHolds = (sierraItemIdentifiers, holdStatuses, stacksPickups)
      .zipped.toList map {

      case (sierraItemIdentifier, holdStatus, stacksPickup) =>
        StacksHold(
          itemId = sierraItemIdentifier,
          pickup = stacksPickup,
          status = holdStatus
        )

    }

  } yield StacksUserHolds(
    userId = userId.value,
    holds = userHolds,
  )

  def placeHold(
                 userIdentifier: StacksUserIdentifier,
                 sierraItemIdentifier: SierraItemIdentifier,
                 itemLocation: StacksLocation
               ): Future[Unit] = for {
    patronsApi <- patronsApi()
    patronHoldPost = new PatronHoldPost()
    _ = patronHoldPost.setRecordType("i")
    // TODO: Deal with this not being Longable
    _ = patronHoldPost.setRecordNumber(sierraItemIdentifier.value.toLong)
    _ = patronHoldPost.setPickupLocation(itemLocation.id)

    _ = patronsApi.placeANewHoldRequest(patronHoldPost, userIdentifier.value.toInt)
  } yield ()

  def getItemStatus(sierraId: SierraItemIdentifier): Future[StacksItemStatus] = for {
    itemsApi <- itemsApi()
    sierraItem = itemsApi.getAnItemByRecordID(
      sierraId.value, List.empty[String].asJava
    )
  } yield StacksItemStatus(
    rawCode = sierraItem.getStatus.getCode
  )

}
