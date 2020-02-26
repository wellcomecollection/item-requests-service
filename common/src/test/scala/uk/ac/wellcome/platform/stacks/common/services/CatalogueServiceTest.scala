package uk.ac.wellcome.platform.stacks.common.services

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.platform.stacks.common.fixtures.{
  CatalogueServiceFixtures,
  ServicesFixture
}
import uk.ac.wellcome.platform.stacks.common.models._
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource.{
  SearchStub,
  WorkStub
}

import scala.concurrent.Future
import scala.util.Random

class CatalogueServiceTest
    extends FunSpec
    with ServicesFixture
    with CatalogueServiceFixtures
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
            catalogueService.getStacksItems(stacksWorkIdentifier)
          ) { stacksItems =>
            val expectedItems = List(
                StacksItemIdentifier(
                  catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                  sierraId = SierraItemIdentifier(1292185)
              )
            )

            stacksItems shouldBe expectedItems
          }

        }
      }
    }

    describe("getStacksItem") {

      it("should filter non-matching items from the source") {
        withActorSystem { implicit as =>
          implicit val ec = as.dispatcher

          val location = createPhysicalLocation(
            id = "sicon",
            label = "Closed stores Iconographic"
          )
          val catalogueId = Random.nextString(10)
          val sierraId = Random.nextLong()

          val items = List(
            createItem(
              catId = catalogueId,
              sierraId = sierraId,
              locations = List(location)
            ),
            createItem()
          )

          val catalogueService = new CatalogueService(
            new CatalogueSource() {
              def getWorkStub(id: StacksWorkIdentifier): Future[WorkStub] =
                Future.failed(new NotImplementedError())

              def getSearchStub(identifier: Identifier[_]): Future[SearchStub] =
                Future {
                  SearchStub(
                    totalResults = items.size,
                    results = List(
                      WorkStub(
                        id = Random.nextString(10),
                        items = items
                      )
                    )
                  )
                }
            }
          )

          val eventuallyStacksItem = catalogueService.getStacksItem(
            StacksWorkIdentifier(catalogueId)
          )

          whenReady(eventuallyStacksItem) { maybeStacksItemId =>
            val stacksItemId = maybeStacksItemId.get

            stacksItemId shouldBe StacksItemIdentifier(
              CatalogueItemIdentifier(catalogueId),
              SierraItemIdentifier(sierraId)
            )
          }
        }
      }

      it("should get a StacksItem for a SierraItemIdentifier") {
        withCatalogueService { catalogueService =>
          val itemIdentifier = SierraItemIdentifier(1292185)

          whenReady(
            catalogueService.getStacksItem(itemIdentifier)
          ) { maybeStacksItemIdentifier =>
            val expectedStacksItemIdentifier = Some(StacksItemIdentifier(
                catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                sierraId = SierraItemIdentifier(1292185)
              )
            )

            maybeStacksItemIdentifier shouldBe expectedStacksItemIdentifier
          }
        }
      }

      it("should get a StacksItem for a CatalogueItemIdentifier") {
        withCatalogueService { catalogueService =>
          val itemIdentifier = CatalogueItemIdentifier("ys3ern6x")

          whenReady(
            catalogueService.getStacksItem(itemIdentifier)
          ) { maybeStacksItemIdentifier =>
            val expectedStacksItemIdentifier = Some(
              StacksItemIdentifier(
                catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                sierraId = SierraItemIdentifier(1292185)
              )
            )

            maybeStacksItemIdentifier shouldBe expectedStacksItemIdentifier
          }
        }
      }
    }
  }

}
