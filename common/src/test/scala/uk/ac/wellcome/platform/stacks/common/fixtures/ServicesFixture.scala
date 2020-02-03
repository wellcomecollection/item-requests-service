package uk.ac.wellcome.platform.stacks.common.fixtures

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import com.github.tomakehurst.wiremock.WireMockServer
import uk.ac.wellcome.akka.fixtures.Akka
import uk.ac.wellcome.fixtures.TestWith
import uk.ac.wellcome.platform.stacks.common.services.{CatalogueService, SierraServiceOld, SierraService, StacksService}

import scala.concurrent.ExecutionContextExecutor

trait ServicesFixture
  extends Akka
    with SierraWireMockFixture
    with CatalogueWireMockFixture  {

  def withCatalogueService[R](
                               testWith: TestWith[CatalogueService, R]
                             ): R = {
    withMockCatalogueServer { catalogueApiUrl: String =>
      withActorSystem { implicit as =>
        withMaterializer { implicit mat =>
          testWith(new CatalogueService(Some(Uri(
            s"$catalogueApiUrl/catalogue/v2"
          ))))
        }
      }
    }
  }

  def withSierraService[R](
                            testWith: TestWith[(SierraService, WireMockServer), R]
                          ): R = {
    withMockSierraServer { case (sierraApiUrl, wireMockServer) =>
      withActorSystem { implicit as =>
        implicit val ec: ExecutionContextExecutor = as.dispatcher

        withMaterializer { implicit mat =>
          testWith(
            (
              new SierraService(
                maybeBaseUri = Some(Uri(f"$sierraApiUrl/iii/sierra-api")),
                credentials = BasicHttpCredentials("username", "password")
              ),
              wireMockServer
            )
          )
        }
      }
    }
  }

  def withSierraServiceOld[R](
                               testWith: TestWith[(SierraServiceOld, WireMockServer), R]
                             ): R = {
    withMockSierraServer { case (sierraApiUrl, wireMockServer) =>
      withActorSystem { implicit as =>
        implicit val ec: ExecutionContextExecutor = as.dispatcher

        withMaterializer { implicit mat =>
          testWith(
            (
              new SierraServiceOld(
                baseUrl = Some(f"$sierraApiUrl/iii/sierra-api"),
                username = "username",
                password = "password"
              ),
              wireMockServer
            )
          )
        }
      }
    }
  }

  def withStacksService[R](
                            testWith: TestWith[(StacksService, WireMockServer), R]
                          ): R = {
    withCatalogueService { catalogueService =>
      withSierraServiceOld { case (sierraService, sierraWireMockSerever) =>
        withActorSystem { implicit as =>
          implicit val ec: ExecutionContextExecutor = as.dispatcher

          val stacksService = new StacksService(catalogueService, sierraService)

          testWith(
            (stacksService, sierraWireMockSerever)
          )
        }
      }
    }
  }
}