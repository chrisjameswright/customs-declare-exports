/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.exports.services.mapping

import javax.inject.Inject
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import uk.gov.hmrc.exports.models.declaration.ExportsDeclaration
import uk.gov.hmrc.exports.services.mapping.declaration.DeclarationBuilder
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.documentmetadata_dms._2.MetaData
import wco.datamodel.wco.metadata_ds_dms._2._

class SubmissionMetaDataBuilder @Inject()(declarationBuilder: DeclarationBuilder) {

  val functionCode = "9"
  val wcoDataModelVersionCode = "3.6"
  val wcoTypeName = "DEC"
  val responsibleCountryCode = "GB"
  val responsibleAgencyName = "HMRC"
  val agencyAssignedCustomizationVersionCode = "v2.1"

  def build(model: ExportsDeclaration): MetaData = {
    val metaData = createMetaDataWithConstants()

    val element: JAXBElement[Declaration] = new JAXBElement[Declaration](
      new QName("urn:wco:datamodel:WCO:DEC-DMS:2", "Declaration"),
      classOf[Declaration],
      declarationBuilder.buildDeclaration(model)
    )
    metaData.setAny(element)
    metaData
  }

  private def createMetaDataWithConstants(): MetaData = {
    val metaData = new MetaData
    val agencyAssignedCustomizationCodeType = new MetaDataAgencyAssignedCustomizationCodeType
    agencyAssignedCustomizationCodeType.setValue(agencyAssignedCustomizationVersionCode)

    val metaDataResponsibleAgencyNameTextType = new MetaDataResponsibleAgencyNameTextType
    metaDataResponsibleAgencyNameTextType.setValue(responsibleAgencyName)

    val metaDataResponsibleCountryCodeType = new MetaDataResponsibleCountryCodeType
    metaDataResponsibleCountryCodeType.setValue(responsibleCountryCode)

    val metaDataWCODataModelVersionCodeType = new MetaDataWCODataModelVersionCodeType
    metaDataWCODataModelVersionCodeType.setValue(wcoDataModelVersionCode)

    val metaDataWCOTypeNameTextType = new MetaDataWCOTypeNameTextType
    metaDataWCOTypeNameTextType.setValue(wcoTypeName)

    metaData.setAgencyAssignedCustomizationCode(agencyAssignedCustomizationCodeType)
    metaData.setResponsibleAgencyName(metaDataResponsibleAgencyNameTextType)
    metaData.setResponsibleCountryCode(metaDataResponsibleCountryCodeType)
    metaData.setWCODataModelVersionCode(metaDataWCODataModelVersionCodeType)
    metaData.setWCOTypeName(metaDataWCOTypeNameTextType)
    metaData
  }

}
