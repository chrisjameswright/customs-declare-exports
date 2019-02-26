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

package uk.gov.hmrc.exports.controllers

import org.mockito.Mockito.{reset, times, verify}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.mvc.Codec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.exports.base.{CustomsExportsBaseSpec, ExportsTestData}
import uk.gov.hmrc.exports.models.{DeclarationMetadata, DeclarationNotification}
import uk.gov.hmrc.wco.dec.{DateTimeString, Response, ResponseDateTimeElement}

class NotificationsControllerSpec extends CustomsExportsBaseSpec with ExportsTestData {

  val uri = "/customs-declare-exports/notify"
  val movementUri = "/customs-declare-exports/notifyMovement"
  val submissionNotificationUri = "/customs-declare-exports/submission-notifications/1234"

  val getNotificationUri = "/customs-declare-exports/notifications"
  val postNotificationUri = "/customs-declare-exports/notify"
  val validXML = <MetaData xmlns="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2">
    <wstxns1:Response xmlns:wstxns1="urn:wco:datamodel:WCO:RES-DMS:2"></wstxns1:Response>
  </MetaData>

  val movementXml = <inventoryLinkingMovementRequest xmlns="http://gov.uk/customs/inventoryLinking/v1">
    <messageCode>EAL</messageCode>
  </inventoryLinkingMovementRequest>

  val validHeaders = Map(
    "X-CDS-Client-ID" -> "1234",
    "X-Conversation-ID" -> "XConv1",
    "X-EORI-Identifier" -> "eori1",
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    "X-Badge-Identifier" -> "badgeIdentifier1"
  )

  val noEoriHeaders = Map(
    "X-CDS-Client-ID" -> "1234",
    "X-Conversation-ID" -> "XConv1",
    HeaderNames.ACCEPT -> s"application/vnd.hmrc.${2.0}+xml",
    HeaderNames.CONTENT_TYPE -> ContentTypes.XML(Codec.utf_8),
    "X-Badge-Identifier" -> "badgeIdentifier1"
  )

  val submissionNotification =
    DeclarationNotification(conversationId = "1234", eori = "eori", metadata = DeclarationMetadata())

  "NotificationsControllerSpec" should {
    "successfully accept Notification" in {
      withNotificationSaved(true)
      withSubmissionNotification(Seq.empty)

      val result = route(app, FakeRequest(POST, uri).withHeaders(validHeaders.toSeq: _*).withXmlBody(validXML)).get

      status(result) must be(ACCEPTED)
    }

    "failed to save Notification" in {
      withNotificationSaved(false)
      withSubmissionNotification(Seq.empty)

      val result = route(app, FakeRequest(POST, uri).withHeaders(validHeaders.toSeq: _*).withXmlBody(validXML)).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "500 response if no eori header in Notification" in {
      val result = route(app, FakeRequest(POST, uri).withHeaders(noEoriHeaders.toSeq: _*).withXmlBody(validXML)).get

      status(result) must be(INTERNAL_SERVER_ERROR)
      contentAsString(result) must be
      ("<errorResponse><code>INTERNAL_SERVER_ERROR</code><message>" +
        "ClientId or ConversationId or EORI is missing in the request headers</message></errorResponse>")
    }

    "get Notifications" in {
      withAuthorizedUser()
      haveNotifications(Seq(notification))

      val result = route(app, FakeRequest(GET, getNotificationUri)).get

      status(result) must be(OK)
    }

    "successfully saved movement notification" in {
      withMovementNotificationSaved(true)

      val result =
        route(app, FakeRequest(POST, movementUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(movementXml)).get

      status(result) must be(ACCEPTED)
    }

    "failed to save movement notification" in {
      withMovementNotificationSaved(false)

      val result =
        route(app, FakeRequest(POST, movementUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(movementXml)).get

      status(result) must be(INTERNAL_SERVER_ERROR)
    }

    "500 response if no eori header in movement notification" in {
      val result =
        route(app, FakeRequest(POST, movementUri).withHeaders(noEoriHeaders.toSeq: _*).withXmlBody(movementXml)).get

      status(result) must be(INTERNAL_SERVER_ERROR)
      contentAsString(result) must be
      ("<errorResponse><code>INTERNAL_SERVER_ERROR</code><message>" +
        "ClientId or ConversationId or EORI is missing in the request headers</message></errorResponse>")
    }

    "return 200 status and notifications relates with specific submission" in {
      withAuthorizedUser()
      withSubmissionNotification(Seq(submissionNotification))

      val result = route(app, FakeRequest(GET, submissionNotificationUri).withHeaders(validHeaders.toSeq: _*)).get

      status(result) must be(OK)
    }

    "return 204 when there is no notifications related with submission" in {
      withAuthorizedUser()
      withSubmissionNotification(Seq.empty)

      val result = route(app, FakeRequest(GET, submissionNotificationUri).withHeaders(validHeaders.toSeq: _*)).get

      status(result) must be(OK)
    }
  }

  val notificationXML =
    <MetaData xmlns="urn:wco:datamodel:WCO:DocumentMetaData-DMS:2"
                xmlns:response="urn:wco:datamodel:WCO:RES-DMS:2"
                xmlns:responseDs="urn:wco:datamodel:WCO:Response_DS:DMS:2">
        <WCODataModelVersionCode>02</WCODataModelVersionCode>
        <WCOTypeName>RES</WCOTypeName>
        <response:Response>
          <response:FunctionCode>02</response:FunctionCode>
          <response:FunctionalReferenceID>1234555</response:FunctionalReferenceID>
          <response:IssueDateTime>
            <responseDs:DateTimeString formatCode="304">20190226085021Z</responseDs:DateTimeString>
          </response:IssueDateTime>
        </response:Response>
        <response:Declaration></response:Declaration>
      </MetaData>

  val olderNotification = DeclarationNotification(
    conversationId = "convId",
    eori = "eori",
    metadata = DeclarationMetadata(),
    response = Seq(
      Response(functionCode = "02", issueDateTime = Some(ResponseDateTimeElement(DateTimeString("102", "20190224"))))
    )
  )

  val newerNotification = DeclarationNotification(
    conversationId = "convId",
    eori = "eori",
    metadata = DeclarationMetadata(),
    response = Seq(
      Response(functionCode = "02", issueDateTime = Some(ResponseDateTimeElement(DateTimeString("102", "20190227"))))
    )
  )

  "saving notification" should {
    "update submission status when there are no notifications" in {
      reset(mockSubmissionRepository)
      withAuthorizedUser()
      withSubmissionNotification(Seq.empty)
      withNotificationSaved(true)

      val result =
        route(
          app,
          FakeRequest(POST, postNotificationUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(notificationXML)
        ).get

      status(result) must be(ACCEPTED)
      verify(mockSubmissionRepository, times(1)).updateStatus("eori1", "XConv1", Some("02"))
    }

    "update submission status when notification is older than exist one" in {
      reset(mockSubmissionRepository)
      withAuthorizedUser()
      withSubmissionNotification(Seq(olderNotification))
      withNotificationSaved(true)

      val result =
        route(
          app,
          FakeRequest(POST, postNotificationUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(notificationXML)
        ).get

      status(result) must be(ACCEPTED)
      verify(mockSubmissionRepository, times(1)).updateStatus("eori1", "XConv1", Some("02"))
    }

    "not update submission when notification than exist is older than new one" in {
      reset(mockSubmissionRepository)
      withAuthorizedUser()
      withSubmissionNotification(Seq(newerNotification))
      withNotificationSaved(true)

      val result =
        route(
          app,
          FakeRequest(POST, postNotificationUri).withHeaders(validHeaders.toSeq: _*).withXmlBody(notificationXML)
        ).get

      status(result) must be(ACCEPTED)
      verify(mockSubmissionRepository, times(0)).updateStatus("eori1", "XConv1", Some("02"))
    }
  }
}
