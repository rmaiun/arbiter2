package dev.rmaiun.soos.db.queries

import cats.data.NonEmptyList
import dev.rmaiun.soos.db.entities.{ Algorithm, Realm }
import doobie.Fragments
import doobie.implicits._

object AlgorithmQueries {

  def getById(id: Long): doobie.Query0[Algorithm] =
    sql"select * from algorithm where algorithm.id = $id limit 1"
      .query[Algorithm]

  def getByValue(value: String): doobie.Query0[Algorithm] =
    sql"select * from algorithm where algorithm.value = $value limit 1"
      .query[Algorithm]

  def insert(algorithm: Algorithm): doobie.Update0 =
    sql"""
         | insert into algorithm (id,value)
         | values (${algorithm.id}, ${algorithm.value})
        """.stripMargin.update

  def update(algorithm: Algorithm): doobie.Update0 =
    sql"""
         | update algorithm
         | set value=${algorithm.value}
         | where id = ${algorithm.id}
    """.stripMargin.update

  def listAll: doobie.Query0[Algorithm] =
    sql"select * from algorithm"
      .query[Algorithm]

  def deleteByIdList(idList: List[Long]): doobie.Update0 =
    NonEmptyList.fromList(idList) match {
      case Some(ids) =>
        val query = fr"delete from algorithm where" ++ Fragments.in(fr" algorithm.id", ids)
        query.update
      case None =>
        sql"delete from algorithm".update
    }
}
