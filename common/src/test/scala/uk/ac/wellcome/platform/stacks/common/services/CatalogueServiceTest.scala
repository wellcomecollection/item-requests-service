package uk.ac.wellcome.platform.stacks.common.services

import akka.http.scaladsl.model.Uri
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.platform.stacks.common.fixtures.ServicesFixture
import uk.ac.wellcome.platform.stacks.common.models._
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource.{IdentifiersStub, ItemStub, LocationStub, SearchStub, TypeStub, WorkStub}
import uk.ac.wellcome.platform.stacks.common.services.source.{AkkaCatalogueSource, CatalogueSource}
import uk.ac.wellcome.storage.generators.RandomThings

import scala.concurrent.Future

class CatalogueServiceTest
    extends FunSpec
    with ServicesFixture
    with ScalaFutures
    with IntegrationPatience
    with RandomThings
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

          val interestingCatalogueId = randomAlphanumeric
          val interestingSierraId = randomInt(1, 1000).toLong
          val interestingLocation = LocationStub(
            locationType = TypeStub(
              id = "sicon",
              label = "Closed stores Iconographic"
            ),
            label = Some("Closed stores Iconographic"),
            `type` = "PhysicalLocation"
          )

          val catalogueService = new CatalogueService(
            new CatalogueSource() {
              def getWorkStub(id: StacksWorkIdentifier): Future[WorkStub] =
                Future.failed(new NotImplementedError())

              def getSearchStub(identifier: Identifier[_]): Future[SearchStub] = {
                def createItem(catId: String, sierraId: Long) = ItemStub(
                  id = catId,
                  identifiers = List(
                    IdentifiersStub(
                      identifierType = TypeStub(
                        id = "sierra-identifier",
                        label = "Sierra identifier"
                      ),
                      value = sierraId.toString
                    )
                  ),
                  locations = List(interestingLocation)
                )

                val items = List(
                  createItem(interestingCatalogueId, interestingSierraId),
                  createItem(randomAlphanumeric, randomInt(1, 1000).toLong)
                )

                Future {
                  SearchStub(
                    totalResults = items.size,
                    results = List(
                      WorkStub(
                        id = randomAlphanumeric,
                        items = items
                      )
                    )
                  )
                }
              }
            }
          )

          val eventuallyStacksItem = catalogueService.getStacksItem(
            StacksWorkIdentifier(interestingCatalogueId)
          )

          whenReady(eventuallyStacksItem) { maybeStacksItem =>
            val stacksItem = maybeStacksItem.get

            stacksItem shouldBe StacksItemWithOutStatus(
              id = StacksItemIdentifier(
                CatalogueItemIdentifier(
                  interestingCatalogueId
                ),
                SierraItemIdentifier(
                  interestingSierraId
                )
              ),
              location = StacksLocation(
                id = interestingLocation.locationType.id,
                label = interestingLocation.locationType.label
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
