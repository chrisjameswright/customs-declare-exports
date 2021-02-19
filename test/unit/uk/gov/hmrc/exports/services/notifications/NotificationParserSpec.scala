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

package uk.gov.hmrc.exports.services.notifications

import testdata.ExportsTestData.mrn
import testdata.notifications.ExampleXmlAndNotificationDetailsPair._
import uk.gov.hmrc.exports.base.UnitSpec
import uk.gov.hmrc.exports.models.declaration.notifications.NotificationDetails

class NotificationParserSpec extends UnitSpec {

  "NotificationParser on parse" when {

    "provided with notification containing no Response element" should {

      "return empty sequence" in {
        val xml = dataForEmptyNotification(mrn).asXml

        val result = NotificationParser.parse(xml)

        result mustBe Seq.empty[NotificationDetails]
      }
    }

    "provided with notification containing single Response element" which {

      "contains no error elements" should {

        "return single NotificationDetails with empty errors" in {
          val testNotification = dataForReceivedNotification(mrn)

          val result = NotificationParser.parse(testNotification.asXml)

          result mustBe testNotification.asDomainModel
        }
      }

      "contains error elements" should {

        "return single NotificationDetails with errors" in {
          val testNotification = dataForRejectedNotification(mrn)

          val result = NotificationParser.parse(testNotification.asXml)

          result mustBe testNotification.asDomainModel
        }
      }
    }

    "provided with notification containing multiple Response elements" should {

      "return multiple NotificationDetails" in {
        val testNotification = dataForNotificationWithMultipleResponses(mrn)

        val result = NotificationParser.parse(testNotification.asXml)

        result mustBe testNotification.asDomainModel
      }
    }

    "provided with notification containing invalid element" should {

      "throw an Exception" in {
        val testNotification = dataForUnparsableNotification(mrn)

        an[Exception] mustBe thrownBy(NotificationParser.parse(testNotification.asXml))
      }
    }
  }

}
