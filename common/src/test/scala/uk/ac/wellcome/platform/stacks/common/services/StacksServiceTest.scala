package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import com.github.tomakehurst.wiremock.client.WireMock.{equalToJson, postRequestedFor, urlEqualTo}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.platform.stacks.common.fixtures.ServicesFixture
import uk.ac.wellcome.platform.stacks.common.models._

class StacksServiceTest
  extends FunSpec
    with ServicesFixture
    with ScalaFutures
    with IntegrationPatience
    with Matchers {

  describe("StacksService") {
    describe("requestHoldOnItem") {
      it("should request a hold from the Sierra API") {
        withStacksService { case (stacksService, wireMockServer) =>
          val stacksUserIdentifier = StacksUser("1234567")
          val catalogueItemIdentifier = CatalogueItemIdentifier("ys3ern6x")

          whenReady(
            stacksService.requestHoldOnItem(
              userIdentifier = stacksUserIdentifier,
              catalogueItemId = catalogueItemIdentifier
            )
          ) { stacksHoldRequest =>

            stacksHoldRequest shouldBe StacksHoldRequest(
              itemId = "ys3ern6x",
              userId = "1234567"
            )

            wireMockServer.verify(1, postRequestedFor(
              urlEqualTo("/iii/sierra-api/v5/patrons/1234567/holds/requests")
            ).withRequestBody(equalToJson(
              """
                |{
                |  "recordType" : "i",
                |  "recordNumber" : 1292185,
                |  "pickupLocation" : "sicon"
                |}
                |""".stripMargin)
            ))
          }
        }
      }
    }

    describe("getStacksWorkWithItemStatuses") {
      it("gets a StacksWork[StacksItemWithStatus]") {
        withStacksService { case (stacksService, _) =>
          val stacksWorkIdentifier = StacksWorkIdentifier("cnkv77md")

          whenReady(
            stacksService.getStacksWorkWithItemStatuses(
              workId = stacksWorkIdentifier
            )
          ) { stacksWork =>

            stacksWork shouldBe StacksWork(
              id = "cnkv77md",
              items = List(
                StacksItemWithStatus(
                  id = StacksItemIdentifier(
                    catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                    sierraId = SierraItemIdentifier("1292185")),
                  location = StacksLocation("sicon", "Closed stores Iconographic"),
                  status = StacksItemStatus("available", "Available")
                )
              )
            )

          }
        }
      }
    }

    describe("getStacksUserHoldsWithStacksItemIdentifier") {
      it("gets a StacksUserHolds[StacksItemIdentifier]") {
        withStacksService { case (stacksService, _) =>
          val stacksUserIdentifier = StacksUser("1234567")

          whenReady(
            stacksService.getStacksUserHoldsWithStacksItemIdentifier(
              userId = stacksUserIdentifier
            )
          ){ stacksUserHolds =>

            stacksUserHolds shouldBe StacksUserHolds(
              userId = "1234567",
              holds = List(
                StacksHold(
                  itemId = StacksItemIdentifier(
                    catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                    sierraId = SierraItemIdentifier("1292185")
                  ),
                  pickup = StacksPickup(
                    location = StacksLocation("sepbb", "Rare Materials Room"),
                    pickUpBy = Instant.parse("2019-12-03T04:00:00Z")
                  ),
                  status = StacksHoldStatus("i", "item hold ready for pickup.")
                )
              )
            )

          }
        }
      }
    }
  }
}

