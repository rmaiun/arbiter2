//package dev.rmaiun.datamanager.repositories
//
//import cats.Monad
//import cats.effect.Sync
//import com.mairo.ukl.domains.Season
//import com.mairo.ukl.domains.queries.SeasonQueries
//import com.mairo.ukl.utils.flow
//import com.mairo.ukl.utils.flow.Flow.Flow
//import com.mairo.ukl.utils.flow.Flow
//import doobie.hikari.HikariTransactor
//import doobie.implicits._
//import io.chrisdavenport.log4cats.Logger
//
//trait SeasonRepository[F[_]]{
//
//}
//
//object SeasonRepository {
//  def apply[F[_]](implicit ev: SeasonRepository[F]): SeasonRepository[F] = ev
//
//  def impl[F[_] : Logger : Sync : Monad](xa: HikariTransactor[F]): SeasonRepository[F] = new SeasonRepository[F] {
//    override def getByName(name: String): Flow[F, Option[Season]] = {
//      val result = SeasonQueries.getSeasonByName(name)
//        .option
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      flow.Flow(result)
//    }
//
//    override def listAll: Flow[F, List[Season]] = {
//      val result = SeasonQueries.findAllSeasons
//        .to[List]
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      flow.Flow(result)
//    }
//
//    override def getById(id: Long): Flow[F, Option[Season]] = {
//      val result = SeasonQueries.getSeasonById(id)
//        .option
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      flow.Flow(result)
//    }
//
//    override def insert(name: String): Flow[F, Long] = {
//      val result = SeasonQueries.insertSeason(name)
//        .withUniqueGeneratedKeys[Long]("id")
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      flow.Flow(result)
//    }
//
//    override def update(data: Season): Flow[F, Season] = {
//      val result = SeasonQueries.updatSeason(data)
//        .run
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      Flow(Monad[F].map(result)(e => e.map(v => data)))
//    }
//
//    override def deleteById(id: Long): Flow[F, Long] = {
//      val result = SeasonQueries.deleteSeasonById(id)
//        .run
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      Flow(Monad[F].map(result)(_.map(_ => id)))
//    }
//
//    override def clearTable: Flow[F, Int] = {
//      val result = SeasonQueries.clearTable
//        .run
//        .transact(xa)
//        .attemptSql
//        .adaptError
//      flow.Flow(result)
//    }
//  }
//}