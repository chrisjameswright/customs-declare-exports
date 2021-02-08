/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.exports.connector

import java.util.UUID

import scala.concurrent.Future

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import stubs.ExternalServicesConfig.{Host, Port}
import stubs.{CustomsDeclarationsAPIConfig, CustomsDeclarationsAPIService}
import testdata.ExportsDeclarationBuilder
import testdata.ExportsTestData._
import uk.gov.hmrc.exports.base.IntegrationTestSpec
import uk.gov.hmrc.exports.connectors.CustomsDeclarationsConnector
import uk.gov.hmrc.exports.util.TestModule
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

class CustomsDeclarationsConnectorSpec
    extends IntegrationTestSpec with GuiceOneAppPerSuite with CustomsDeclarationsAPIService with ExportsDeclarationBuilder {

  private lazy val connector = app.injector.instanceOf[CustomsDeclarationsConnector]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override implicit lazy val app: Application =
    GuiceApplicationBuilder(overrides = Seq(TestModule.asGuiceableModule))
      .configure(
        Map(
          "microservice.services.customs-declarations.host" -> Host,
          "microservice.services.customs-declarations.port" -> Port,
          "microservice.services.customs-declarations.submit-uri" -> CustomsDeclarationsAPIConfig.submitDeclarationServiceContext,
          "microservice.services.customs-declarations.bearer-token" -> authToken,
          "microservice.services.customs-declarations.api-version" -> CustomsDeclarationsAPIConfig.apiVersion
        )
      )
      .build()

  // TODO: added couple of tests for having convId or not, seems like there is logic which even if we got 202 but not convId
  // TODO: it will be wrapped into 500 (to be confirmed)
  // TODO: do we handle cancellations now ? as if so we would need to add tests here
  "Customs Declarations Connector" should {

    "return response with specific status" when {

      "request is processed successfully - 202" in {

        startSubmissionService(ACCEPTED, UUID.randomUUID.toString)
        await(sendValidXml(expectedSubmissionRequestPayload("123")))

        verifyDecServiceWasCalledCorrectly(
          requestBody = expectedSubmissionRequestPayload("123"),
          expectedEori = declarantEoriValue,
          expectedApiVersion = CustomsDeclarationsAPIConfig.apiVersion
        )
      }

      "request is processed successfully (external 202), but does not have conversationId - 500" in {

        startSubmissionService(ACCEPTED, conversationId = "")
        intercept[InternalServerException] {
          await(sendValidXml(expectedSubmissionRequestPayload("123")))
        }
      }

      "request is not processed - 500" in {

        startSubmissionService(INTERNAL_SERVER_ERROR, UUID.randomUUID.toString)
        intercept[InternalServerException] {
          await(sendValidXml(expectedSubmissionRequestPayload("123")))
        }
      }
    }
  }

  private def sendValidXml(xml: String): Future[String] =
    connector.submitDeclaration(declarantEoriValue, xml)
}
