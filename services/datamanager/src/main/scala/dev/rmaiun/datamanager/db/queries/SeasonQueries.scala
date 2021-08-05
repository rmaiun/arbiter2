//package dev.rmaiun.datamanager.db.queries
//
//import dev.rmaiun.datamanager.db.entities.Season
//import doobie.implicits._
//
//object SeasonQueries {
//
//  def findAllSeasons: doobie.Query0[Season] = {
//    sql"SELECT * FROM season"
//      .query[Season]
//  }
//
//  def deleteSeasonById(id: Long): doobie.Update0 = {
//    sql"DELETE FROM season where season.id = $id"
//      .update
//  }
//
//  def clearTable: doobie.Update0 = {
//    sql"DELETE FROM season"
//      .update
//  }
//
//  def getSeasonById(id: Long): doobie.Query0[Season] = {
//    sql"SELECT * FROM season WHERE season.id = $id LIMIT 1"
//      .query[Season]
//  }
//
//  def getSeasonByName(name: String): doobie.Query0[Season] = {
//    sql"SELECT * FROM season WHERE season.name = $name LIMIT 1"
//      .query[Season]
//  }
//
//  def insertSeason(name: String): doobie.Update0 = {
//    sql"INSERT into season (name) VALUES ($name)"
//      .update
//  }
//
//  def updatSeason(season: Season): doobie.Update0 = {
//    sql"""
//         |UPDATE season
//         | SET name=${season.name}
//         | WHERE id = ${season.id}
//    """.stripMargin
//      .update
//  }
//}
