package uk.ac.wellcome.platform.stacks.common.services

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.platform.stacks.common.fixtures.{CatalogueServiceFixtures, CatalogueStubGenerators, ServicesFixture}
import uk.ac.wellcome.platform.stacks.common.models._
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource
import uk.ac.wellcome.platform.stacks.common.services.source.CatalogueSource._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class CatalogueServiceTest
    extends FunSpec
    with ServicesFixture
    with CatalogueServiceFixtures
    with ScalaFutures
    with IntegrationPatience
    with Matchers
    with CatalogueStubGenerators {

  describe("getAllStacksItems") {
    class OneWorkCatalogueSource(work: WorkStub) extends CatalogueSource {
      override def getWorkStub(id: StacksWorkIdentifier): Future[WorkStub] =
        Future.successful(work)

      override def getSearchStub(identifier: Identifier[_]): Future[SearchStub] =
        Future.failed(new Throwable("BOOM!"))
    }

    it("finds an item on a work") {
      val item = ItemStub(
        id = Some(createStacksItemIdentifier.value),
        identifiers = Some(List(createSierraIdentifier("1234567")))
      )

      val work = createWorkStubWith(
        items = List(item)
      )

      val catalogueSource = new OneWorkCatalogueSource(work)
      val service = new CatalogueService(catalogueSource)

      whenReady(service.getAllStacksItems(createStacksWorkIdentifier)) {
        _ shouldBe List(
          StacksItemIdentifier(
            catalogueId = CatalogueItemIdentifier(item.id.get),
            sierraId = SierraItemIdentifier(1234567)
          )
        )
      }
    }

    it("finds multiple matching items on a work") {
      val item1 = ItemStub(
        id = Some(createStacksItemIdentifier.value),
        identifiers = Some(List(createSierraIdentifier("1234567"))
        )
      )

      val item2 = ItemStub(
        id = Some(createStacksItemIdentifier.value),
        identifiers = Some(List(createSierraIdentifier("1111111"))
        )
      )

      val work = createWorkStubWith(
        items = List(item1, item2)
      )

      val catalogueSource = new OneWorkCatalogueSource(work)
      val service = new CatalogueService(catalogueSource)

      whenReady(service.getAllStacksItems(createStacksWorkIdentifier)) {
        _ shouldBe List(
          StacksItemIdentifier(
            catalogueId = CatalogueItemIdentifier(item1.id.get),
            sierraId = SierraItemIdentifier(1234567)
          ),
          StacksItemIdentifier(
            catalogueId = CatalogueItemIdentifier(item2.id.get),
            sierraId = SierraItemIdentifier(1111111)
          )
        )
      }
    }

    it("ignores items that don't have a sierra-identifier") {
      val item = ItemStub(
        id = Some(createStacksItemIdentifier.value),
        identifiers = Some(
          List(
            IdentifiersStub(
              identifierType = TypeStub(id = "miro-image-number", label = "Miro image number"),
              value = "A0001234"
            )
          )
        )
      )

      val work = createWorkStubWith(
        items = List(item)
      )

      val catalogueSource = new OneWorkCatalogueSource(work)
      val service = new CatalogueService(catalogueSource)

      whenReady(service.getAllStacksItems(createStacksWorkIdentifier)) {
        _ shouldBe empty
      }
    }

    it("throws an error if an item has multiple sierra-identifier values") {
      val item = ItemStub(
        id = Some(createStacksItemIdentifier.value),
        identifiers = Some(
          List(
            createSierraIdentifier("1234567"),
            createSierraIdentifier("1111111")
          )
        )
      )

      val work = createWorkStubWith(
        items = List(item)
      )

      val catalogueSource = new OneWorkCatalogueSource(work)
      val service = new CatalogueService(catalogueSource)

      whenReady(service.getAllStacksItems(createStacksWorkIdentifier).failed) { err =>
        err shouldBe a[Exception]
        err.getMessage should startWith("Multiple values for sierra-identifier")
      }
    }

    it("throws an error if it cannot parse the Sierra identifier as a Long") {
      val item = ItemStub(
        id = Some(createStacksItemIdentifier.value),
        identifiers = Some(List(createSierraIdentifier("Not a Number")))
      )

      val work = createWorkStubWith(
        items = List(item)
      )

      val catalogueSource = new OneWorkCatalogueSource(work)
      val service = new CatalogueService(catalogueSource)

      whenReady(service.getAllStacksItems(createStacksWorkIdentifier).failed) { err =>
        err shouldBe a[Exception]
        err.getMessage shouldBe "Unable to convert Not a Number to Long!"
      }
    }
  }

  describe("behaves as a CatalogueService") {
    describe("getStacksItems") {
      ignore("should return a StacksWork") {
        withCatalogueService { catalogueService =>
          val stacksWorkIdentifier = StacksWorkIdentifier(
            "cnkv77md"
          )

          whenReady(
            catalogueService.getAllStacksItems(stacksWorkIdentifier)
          ) { stacksItems =>
            val expectedItems = List(
              StacksItemIdentifier(
                catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                sierraId = SierraItemIdentifier(1601017)
              )
            )

            stacksItems shouldBe expectedItems
          }
        }
      }
    }

    describe("getStacksItem") {

      ignore("should filter non-matching items from the source") {
        withActorSystem { implicit as =>
          implicit val ec = as.dispatcher

          val catalogueId = Random.nextString(10)
          val sierraId = Random.nextLong()

          val items = List(
            createItem(catId = catalogueId, sierraId = sierraId),
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

      ignore("should get a StacksItem for a SierraItemIdentifier") {
        withCatalogueService { catalogueService =>
          val itemIdentifier = SierraItemIdentifier(1292185)

          whenReady(
            catalogueService.getStacksItem(itemIdentifier)
          ) { maybeStacksItemIdentifier =>
            val expectedStacksItemIdentifier = Some(
              StacksItemIdentifier(
                catalogueId = CatalogueItemIdentifier("n5v7b4md"),
                sierraId = SierraItemIdentifier(1292185)
              )
            )

            maybeStacksItemIdentifier shouldBe expectedStacksItemIdentifier
          }
        }
      }

      ignore("should get a StacksItem for a CatalogueItemIdentifier") {
        withCatalogueService { catalogueService =>
          val itemIdentifier = CatalogueItemIdentifier("ys3ern6x")

          whenReady(
            catalogueService.getStacksItem(itemIdentifier)
          ) { maybeStacksItemIdentifier =>
            val expectedStacksItemIdentifier = Some(
              StacksItemIdentifier(
                catalogueId = CatalogueItemIdentifier("ys3ern6x"),
                sierraId = SierraItemIdentifier(1601017)
              )
            )

            maybeStacksItemIdentifier shouldBe expectedStacksItemIdentifier
          }
        }
      }
    }
  }

  private def createSierraIdentifier(value: String): IdentifiersStub =
    IdentifiersStub(
      identifierType = TypeStub(id = "sierra-identifier", label = "Sierra identifier"),
      value = value
    )

}
