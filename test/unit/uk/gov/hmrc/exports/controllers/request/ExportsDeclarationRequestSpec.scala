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

package uk.gov.hmrc.exports.controllers.request

import java.time.Instant

import org.scalatest.{Matchers, MustMatchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.exports.models.declaration.AdditionalDeclarationType.AdditionalDeclarationType
import uk.gov.hmrc.exports.models.declaration._
import uk.gov.hmrc.exports.models.{DeclarationType, Eori}
import testdata.ExportsDeclarationBuilder
import unit.uk.gov.hmrc.exports.models.declaration.ExportsDeclarationSpec

class ExportsDeclarationRequestSpec extends WordSpec with MustMatchers with ExportsDeclarationBuilder with MockitoSugar {

  private val `type` = DeclarationType.STANDARD
  private val createdDate = Instant.MIN
  private val updatedDate = Instant.MAX
  private val sourceId = "source-id"
  private val eori = "eori"
  private val id = "id"
  private val dispatchLocation = mock[DispatchLocation]
  private val additionalDeclarationType = mock[AdditionalDeclarationType]
  private val consignmentReferences = mock[ConsignmentReferences]
  private val departureTransport = mock[DepartureTransport]
  private val borderTransport = mock[BorderTransport]
  private val transportInformation = mock[TransportInformation]
  private val transport = mock[Transport]
  private val parties = mock[Parties]
  private val locations = mock[Locations]
  private val item = mock[ExportItem]
  private val totalNumberOfItems = mock[TotalNumberOfItems]
  private val previousDocuments = mock[PreviousDocuments]
  private val natureOfTransaction = mock[NatureOfTransaction]

  private val request = ExportsDeclarationRequest(
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sourceId = Some(sourceId),
    `type` = `type`,
    dispatchLocation = Some(dispatchLocation),
    additionalDeclarationType = Some(additionalDeclarationType),
    consignmentReferences = Some(consignmentReferences),
    transport = transport,
    parties = parties,
    locations = locations,
    items = Set(item),
    totalNumberOfItems = Some(totalNumberOfItems),
    previousDocuments = Some(previousDocuments),
    natureOfTransaction = Some(natureOfTransaction)
  )

  private val declaration = ExportsDeclaration(
    id = id,
    eori = eori,
    status = DeclarationStatus.DRAFT,
    createdDateTime = createdDate,
    updatedDateTime = updatedDate,
    sourceId = Some(sourceId),
    `type` = `type`,
    dispatchLocation = Some(dispatchLocation),
    additionalDeclarationType = Some(additionalDeclarationType),
    consignmentReferences = Some(consignmentReferences),
    transport = transport,
    parties = parties,
    locations = locations,
    items = Set(item),
    totalNumberOfItems = Some(totalNumberOfItems),
    previousDocuments = Some(previousDocuments),
    natureOfTransaction = Some(natureOfTransaction)
  )

  "Request" should {
    "map to ExportsDeclaration" in {
      request.toExportsDeclaration(id, Eori(eori)) mustBe declaration
    }
  }


  "have json format that parse declaration in version 2" in {
    Json
      .parse(ExportsDeclarationSpec.declarationVersion2)
      .validate[ExportsDeclarationRequest]
      .fold(error => fail(s"Could not parse - $error"), declaration => {
        declaration.transport.borderModeOfTransportCode mustNot be(empty)
        declaration.transport.meansOfTransportOnDepartureType mustNot be(empty)
        declaration.transport.transportPayment mustNot be(empty)
      })
  }

}
