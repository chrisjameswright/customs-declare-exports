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

package uk.gov.hmrc.exports.repositories

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsNull, JsString, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONNull, BSONObjectID}
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.exports.models.declaration.notifications.Notification
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats.objectIdFormats

@Singleton
class NotificationRepository @Inject()(mc: ReactiveMongoComponent)(implicit ec: ExecutionContext)
    extends ReactiveRepository[Notification, BSONObjectID]("notifications", mc.mongoConnector.db, Notification.DbFormat.format, objectIdFormats)
    with RepoHelper {

  override lazy val collection: JSONCollection =
    mongo().collection[JSONCollection](collectionName, failoverStrategy = RepositorySettings.failoverStrategy)

  override def indexes: Seq[Index] = Seq(
    Index(Seq("details.dateTimeIssued" -> IndexType.Ascending), name = Some("detailsDateTimeIssuedIdx")),
    Index(Seq("details.mrn" -> IndexType.Ascending), name = Some("detailsMrnIdx")),
    Index(Seq("actionId" -> IndexType.Ascending), name = Some("actionIdIdx")),
    Index(Seq("details" -> IndexType.Ascending), name = Some("detailsDocMissingIdx"), partialFilter = Some(BSONDocument("details" -> BSONNull)))
  )

  def findNotificationsByActionId(actionId: String): Future[Seq[Notification]] =
    find("actionId" -> JsString(actionId))

  def findNotificationsByActionIds(actionIds: Seq[String]): Future[Seq[Notification]] =
    actionIds match {
      case Seq() => Future.successful(Seq.empty)
      case _     => find("$or" -> actionIds.map(id => Json.obj("actionId" -> JsString(id))))
    }

  def findUnparsedNotifications(): Future[Seq[Notification]] =
    find("details" -> JsNull)

  def removeUnparsedNotificationsForActionId(actionId: String): WriteResponse[Unit] =
    handleWriteResult(remove("actionId" -> JsString(actionId), "details" -> JsNull), ())

  def add(notification: Notification)(implicit ec: ExecutionContext): WriteResponse[Notification] =
    handleWriteResult(insert(notification), notification)
}
