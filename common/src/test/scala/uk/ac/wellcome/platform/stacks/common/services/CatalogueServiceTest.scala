package uk.ac.wellcome.platform.stacks.common.services

import akka.http.scaladsl.model.Uri
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.platform.stacks.common.fixtures.{CatalogueServiceFixtures, ServicesFixture}
import uk.ac.wellcome.platform.stacks.common.models._
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource.{IdentifiersStub, ItemStub, LocationStub, SearchStub, TypeStub, WorkStub}
import uk.ac.wellcome.platform.stacks.common.services.source.{AkkaCatalogueSource, CatalogueSource}
import uk.ac.wellcome.storage.generators.RandomThings

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

          whenReady(eventuallyStacksItem) { maybeStacksItem =>
            val stacksItem = maybeStacksItem.get

            stacksItem shouldBe StacksItemWithOutStatus(
              id = StacksItemIdentifier(
                CatalogueItemIdentifier(
                  catalogueId
                ),
                SierraItemIdentifier(
                  sierraId
                )
              ),
              location = StacksLocation(
                id = location.locationType.id,
                label = location.locationType.label
              )
            )
          }
        }
      }

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
