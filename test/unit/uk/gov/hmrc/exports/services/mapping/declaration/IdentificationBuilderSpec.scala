/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.exports.services.mapping.declaration

import org.scalatest.{MustMatchers, WordSpec}
import wco.datamodel.wco.dec_dms._2.Declaration

class IdentificationBuilderSpec extends WordSpec with MustMatchers {

  private val builder = new IdentificationBuilder()

  "Build then add" should {
    "append to declaration" in {
      val declaration = new Declaration()

      builder.buildThenAdd("id", declaration)

      declaration.getID.getValue mustBe "id"
    }
  }

}
