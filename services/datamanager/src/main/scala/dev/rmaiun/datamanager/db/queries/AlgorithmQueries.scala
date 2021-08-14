package dev.rmaiun.datamanager.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.datamanager.db.entities.{ Algorithm, Realm }
import doobie.Fragments
import doobie.implicits._

object AlgorithmQueries {

  def getById(id: Long): doobie.Query0[Algorithm] =
    sql"SELECT * FROM algorithm WHERE algorithm.id = $id LIMIT 1"
      .query[Algorithm]

  def getByValue(value: String): doobie.Query0[Algorithm] =
    sql"SELECT * FROM algorithm WHERE algorithm.value = $value LIMIT 1"
      .query[Algorithm]

  def insert(algorithm: Algorithm): doobie.Update0 =
    sql"""
         | INSERT into algorithm (id,value)
         | VALUES (${algorithm.id}, ${algorithm.value})
        """.stripMargin.update

  def update(algorithm: Algorithm): doobie.Update0 =
    sql"""
         | UPDATE algorithm
         | SET value=${algorithm.value}
         | WHERE id = ${algorithm.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Algorithm] =
    sql"SELECT * FROM algorithm"
      .query[Algorithm]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"DELETE FROM algorithm where" ++ Fragments.in(fr" algorithm.id", ids)
        query.update
      case None =>
        sql"DELETE FROM algorithm".update
    }
}
