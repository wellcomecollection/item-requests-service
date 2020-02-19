package uk.ac.wellcome.platform.stacks.requests.api

import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import com.github.tomakehurst.wiremock.client.WireMock.{
  equalToJson,
  postRequestedFor,
  urlEqualTo
}
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.{FunSpec, Matchers}
import uk.ac.wellcome.json.utils.JsonAssertions
import uk.ac.wellcome.platform.stacks.common.fixtures.{
  CatalogueWireMockFixture,
  SierraWireMockFixture
}
import uk.ac.wellcome.platform.stacks.requests.api.fixtures.RequestsApiFixture

class RequestsApiFeatureTest
    extends FunSpec
    with Matchers
    with RequestsApiFixture
    with JsonAssertions
    with CatalogueWireMockFixture
    with SierraWireMockFixture
    with IntegrationPatience {

  describe("requests") {
    it("responds to requests containing an Weco-Sierra-Patron-Id header") {
      withMockCatalogueServer { catalogueApiUrl: String =>
        withMockSierraServer {
          case (sierraApiUrl, _) =>
            withConfiguredApp(catalogueApiUrl, sierraApiUrl) {
              case (_, _) =>
                val path = "/requests"

                val headers = List(
                  HttpHeader
                    .parse(
                      name = "Weco-Sierra-Patron-Id",
                      value = "1234567"
                    )
                    .asInstanceOf[ParsingResult.Ok]
                    .header
                )

                whenGetRequestReady(path, headers) { response =>
                  response.status shouldBe StatusCodes.OK
                }
            }
        }
      }
    }

    it("accepts requests to place a hold on an item") {
      withMockCatalogueServer { catalogueApiUrl: String =>
        withMockSierraServer {
          case (sierraApiUrl, wireMockServer) =>
            withConfiguredApp(catalogueApiUrl, sierraApiUrl) {
              case (_, _) =>
                val path = "/requests"

                val headers = List(
                  HttpHeader
                    .parse(
                      name = "Weco-Sierra-Patron-Id",
                      value = "1234567"
                    )
                    .asInstanceOf[ParsingResult.Ok]
                    .header
                )

                val entity = createJsonHttpEntityWith(
                  """
                |{
                |   "itemId": "ys3ern6x"
                |}
                |""".stripMargin
                )

                val expectedJson =
                  """
                |{
                |  "itemId" : "ys3ern6x",
                |  "userId" : "1234567"
                |}
                |""".stripMargin

                whenPostRequestReady(path, entity, headers) { response =>
                  response.status shouldBe StatusCodes.OK

                  wireMockServer.verify(
                    1,
                    postRequestedFor(
                      urlEqualTo(
                        "/iii/sierra-api/v5/patrons/1234567/holds/requests"
                      )
                    ).withRequestBody(equalToJson("""
                  |{
                  |  "recordType" : "i",
                  |  "recordNumber" : 1292185,
                  |  "pickupLocation" : "sicon"
                  |}
                  |""".stripMargin))
                  )

                  withStringEntity(response.entity) { actualJson =>
                    assertJsonStringsAreEqual(actualJson, expectedJson)
                  }
                }
            }
        }
      }
    }

    it("provides information about a users' holds") {
      withMockCatalogueServer { catalogueApiUrl: String =>
        withMockSierraServer {
          case (sierraApiUrl, _) =>
            withConfiguredApp(catalogueApiUrl, sierraApiUrl) {
              case (_, _) =>
                val path = "/requests"

                val headers = List(
                  HttpHeader
                    .parse(
                      name = "Weco-Sierra-Patron-Id",
                      value = "1234567"
                    )
                    .asInstanceOf[ParsingResult.Ok]
                    .header
                )

                val expectedJson =
                  s"""
                 |{
                 |  "results" : [
                 |    {
                 |      "item" : {
                 |        "id" : "ys3ern6x",
                 |        "status" : null,
                 |        "type" : "Item"
                 |      },
                 |      "pickupDate" : "2019-12-03T04:00:00Z",
                 |      "pickupLocation" : {
                 |        "id" : "sepbb",
                 |        "label" : "Rare Materials Room",
                 |        "type" : "LocationDescription"
                 |      },
                 |      "status" : {
                 |        "id" : "i",
                 |        "label" : "item hold ready for pickup.",
                 |        "type" : "RequestStatus"
                 |      },
                 |      "type" : "Request"
                 |    }
                 |  ],
                 |  "totalResults" : 1,
                 |  "type" : "ResultList"
                 |}""".stripMargin

                whenGetRequestReady(path, headers) { response =>
                  response.status shouldBe StatusCodes.OK

                  withStringEntity(response.entity) { actualJson =>
                    assertJsonStringsAreEqual(actualJson, expectedJson)
                  }
                }
            }
        }
      }
    }
  }
}
