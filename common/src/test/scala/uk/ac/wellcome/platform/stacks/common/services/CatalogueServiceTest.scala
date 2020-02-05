package uk.ac.wellcome.platform.stacks.common.services

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.platform.stacks.common.fixtures.ServicesFixture
import uk.ac.wellcome.platform.stacks.common.models._

class CatalogueServiceTest
    extends FunSpec
    with ServicesFixture
    with ScalaFutures
    with IntegrationPatience
    with Matchers {

  describe("CatalogueService") {
    describe("getStacksWork") {
      it("should return a StacksWork") {
        withCatalogueService { catalogueService =>
          val stacksWorkIdentifier = StacksWorkIdentifier(
            "cnkv77md"
          )

          whenReady(
            catalogueService.getStacksWork(stacksWorkIdentifier)
          ) { actualWork =>
            val expectedWork = StacksWork(
              id = "cnkv77md",
              items = List(
                StacksItemWithOutStatus(
                  id = StacksItemIdentifier(
                    catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                    sierraId = SierraItemIdentifier(1292185)
                  ),
                  location =
                    StacksLocation("sicon", "Closed stores Iconographic")
                )
              )
            )

            actualWork shouldBe expectedWork
          }

        }
      }
    }

    describe("getStacksItem") {
      it("should get a StacksItem for a SierraItemIdentifier") {
        withCatalogueService { catalogueService =>
          val itemIdentifier = SierraItemIdentifier(1292185)

          whenReady(
            catalogueService.getStacksItem(itemIdentifier)
          ) { actualWork =>
            val expectedItem = Some(
              StacksItemWithOutStatus(
                id = StacksItemIdentifier(
                  catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                  sierraId = SierraItemIdentifier(1292185)
                ),
                location = StacksLocation("sicon", "Closed stores Iconographic")
              )
            )

            actualWork shouldBe expectedItem
          }
        }
      }

      it("should get a StacksItem for a CatalogueItemIdentifier") {
        withCatalogueService { catalogueService =>
          val itemIdentifier = CatalogueItemIdentifier("ys3ern6x")

          whenReady(
            catalogueService.getStacksItem(itemIdentifier)
          ) { actualWork =>
            val expectedItem = Some(
              StacksItemWithOutStatus(
                id = StacksItemIdentifier(
                  catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                  sierraId = SierraItemIdentifier(1292185)
                ),
                location = StacksLocation("sicon", "Closed stores Iconographic")
              )
            )

            actualWork shouldBe expectedItem
          }
        }
      }
    }
  }

}
