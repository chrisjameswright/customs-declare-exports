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

package uk.gov.hmrc.exports.services.mapping.goodsshipment

import uk.gov.hmrc.exports.base.UnitSpec
import uk.gov.hmrc.exports.models.declaration.NatureOfTransaction
import wco.datamodel.wco.dec_dms._2.Declaration
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class NatureOfTransactionBuilderSpec extends UnitSpec {

  "TransactionTypeBuilder" should {
    "correctly map to the WCO-DEC GoodsShipment.TransactionNatureCodeType instance" when {
      "'identifier' has been supplied" in {
        val builder = new GoodsShipmentNatureOfTransactionBuilder

        val natureOfTransaction: NatureOfTransaction = NatureOfTransaction("1")
        val goodsShipment: Declaration.GoodsShipment = new GoodsShipment

        builder.buildThenAdd(natureOfTransaction, goodsShipment)

        goodsShipment.getTransactionNatureCode.getValue must be("1")
      }
      "'identifier' has not been supplied" in {
        val builder = new GoodsShipmentNatureOfTransactionBuilder

        val natureOfTransaction: NatureOfTransaction = NatureOfTransaction("")
        val goodsShipment: Declaration.GoodsShipment = new GoodsShipment

        builder.buildThenAdd(natureOfTransaction, goodsShipment)

        goodsShipment.getTransactionNatureCode must be(null)
      }
    }
  }
}
