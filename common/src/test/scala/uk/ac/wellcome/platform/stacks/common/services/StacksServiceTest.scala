package uk.ac.wellcome.platform.stacks.common.services

import java.time.Instant

import com.github.tomakehurst.wiremock.client.WireMock.{
  equalToJson,
  postRequestedFor,
  urlEqualTo
}
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
        withStacksService {
          case (stacksService, wireMockServer) =>
            val stacksUserIdentifier = StacksUserIdentifier("1234567")
            val catalogueItemIdentifier = CatalogueItemIdentifier("ys3ern6x")
            val neededBy = Some(
              Instant.parse("2020-01-01T00:00:00.00Z")
            )

            whenReady(
              stacksService.requestHoldOnItem(
                userIdentifier = stacksUserIdentifier,
                catalogueItemId = catalogueItemIdentifier,
                neededBy = neededBy
              )
            ) { stacksHoldRequest =>
              stacksHoldRequest shouldBe StacksHoldRequest(
                itemId = "ys3ern6x",
                userId = "1234567"
              )

              wireMockServer.verify(
                1,
                postRequestedFor(
                  urlEqualTo(
                    "/iii/sierra-api/v5/patrons/1234567/holds/requests"
                  )
                ).withRequestBody(
                  equalToJson("""
                      |{
                      |  "recordType" : "i",
                      |  "recordNumber" : 1292185,
                      |  "pickupLocation" : "unspecified",
                      |  "neededBy" : "2020-01-01"
                      |}
                      |""".stripMargin)
                )
              )
            }
        }
      }
    }

    describe("getStacksWork") {
      it("gets a StacksWork") {
        withStacksService {
          case (stacksService, _) =>
            val workId = StacksWorkIdentifier("cnkv77md")

            whenReady(
              stacksService.getStacksWork(workId)
            ) { stacksWork =>
              stacksWork shouldBe StacksWork(
                id = workId,
                items = List(
                  StacksItem(
                    id = StacksItemIdentifier(
                      catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                      sierraId = SierraItemIdentifier(1292185)
                    ),
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
        withStacksService {
          case (stacksService, _) =>
            val stacksUserIdentifier = StacksUserIdentifier("1234567")

            whenReady(
              stacksService.getStacksUserHolds(
                userId = stacksUserIdentifier
              )
            ) { stacksUserHolds =>
              stacksUserHolds shouldBe StacksUserHolds(
                userId = "1234567",
                holds = List(
                  StacksHold(
                    itemId = StacksItemIdentifier(
                      catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                      sierraId = SierraItemIdentifier(1292185)
                    ),
                    pickup = StacksPickup(
                      location =
                        StacksPickupLocation("sepbb", "Rare Materials Room"),
                      pickUpBy = Some(Instant.parse("2019-12-03T04:00:00Z"))
                    ),
                    status =
                      StacksHoldStatus("i", "item hold ready for pickup.")
                  )
                )
              )
            }
        }
      }
    }
  }
}
