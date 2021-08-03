package dev.rmaiun.mongocore.repositories

import dev.rmaiun.tftypes.types.Flow.Flow
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.conversions.Bson

trait BasicActions[F[_], T] {
  def collection: MongoCollection[T]

  def getById(id: String): Flow[F, Option[T]]

  def listByCriteria(criteria: Bson): Flow[F, Seq[T]]

  def create(e: T): Flow[F, T]

  def update(filter: Bson, update: Bson): Flow[F, T]

  def deleteById(id: String): Flow[F, Long]

  def clearCollection: Flow[F, Long]
}
