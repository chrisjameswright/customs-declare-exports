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

package uk.gov.hmrc.exports.services.mapping.governmentagencygoodsitem

import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.exports.services.mapping.ExportsItemBuilder
import wco.datamodel.wco.dec_dms._2.Declaration.GoodsShipment

class StatisticalValueAmountBuilderSpec extends WordSpec with Matchers with ExportsItemBuilder {

  "Statistical Value Amount Builder" should {
    "build then add" when {
      "empty item type" in {
        val model = anItem(withoutStatisticalValue())
        val dcoItem = new GoodsShipment.GovernmentAgencyGoodsItem()

        builder.buildThenAdd(model, dcoItem)

        dcoItem.getStatisticalValueAmount shouldBe null
      }

      "populated item type" in {
        val model = anItem(withStatisticalValue(statisticalValue = "123.45"))
        val dcoItem = new GoodsShipment.GovernmentAgencyGoodsItem()

        builder.buildThenAdd(model, dcoItem)

        dcoItem.getStatisticalValueAmount.getValue.toString shouldBe "123.45"
        dcoItem.getStatisticalValueAmount.getCurrencyID shouldBe "GBP"
      }
    }
  }

  private def builder = new StatisticalValueAmountBuilder()
}
