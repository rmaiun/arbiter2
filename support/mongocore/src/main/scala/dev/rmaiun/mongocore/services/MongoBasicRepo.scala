package dev.rmaiun.mongocore.services

import cats.Monad
import cats.effect.{Async, ContextShift, Sync}
import dev.rmaiun.mongocore.repositories.BasicActions
import dev.rmaiun.tftypes.types.Flow.{Flow, fromF}
import org.mongodb.scala._
import cats.implicits._
import dev.rmaiun.tftypes.types.Flow
import org.mongodb.scala.bson.conversions.Bson

import scala.reflect.ClassTag

abstract class MongoBasicRepo[F[_]: Async: Monad: ContextShift, T:ClassTag](collectionName: String, mongoDb: MongoDatabase)
    extends BasicActions[F, T] {

  val coll: MongoCollection[T] = mongoDb.getCollection[T](collectionName)


  override def collection:MongoCollection[T] = coll

  override def getById(id: String): Flow[F, Option[T]] = {
    val value = Async.fromFuture(Async[F].delay(coll.find(Document("_id" -> id)).headOption()))
    Flow.fromF(value)
  }

  override def listByCriteria(criteria: Bson): Flow[F, Seq[T]] = {
    val value = Async.fromFuture(Async[F].delay(coll.find(criteria).toFuture()))
    Flow.fromF(value)
  }

  override def create(document: T): Flow[F, T] = {
    val value = Async.fromFuture(Async[F].delay(coll.insertOne(document).toFuture()))
    Flow.fromF(value).map(_ => document)
  }

  override def update(filter: Bson, update: Bson): Flow[F, T] = {
    val value = Async.fromFuture(Async[F].delay(coll.findOneAndUpdate(filter, update).toFuture()))
    Flow.fromF(value)
  }

  override def deleteById(id: String): Flow[F, Long] = {
    val value = Async.fromFuture(Async[F].delay(coll.deleteOne(Document("_id" -> id)).toFuture()))
    Flow.fromF(value).map(_.getDeletedCount)
  }

  override def clearCollection: Flow[F, Long] = {
    val value = Async.fromFuture(Async[F].delay(coll.deleteMany(Document()).toFuture()))
    Flow.fromF(value).map(_.getDeletedCount)
  }
}
