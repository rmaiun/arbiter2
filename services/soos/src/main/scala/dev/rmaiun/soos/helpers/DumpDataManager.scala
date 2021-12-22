package dev.rmaiun.soos.helpers

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.foldable._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.db.PageInfo
import dev.rmaiun.soos.db.entities._
import dev.rmaiun.soos.helpers.DumpDataManager._
import dev.rmaiun.soos.repositories._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._

import java.io.ByteArrayOutputStream
import java.util.zip.{ZipEntry, ZipOutputStream}

case class DumpDataManager[F[_]: Monad: Sync](
  algorithmRepo: AlgorithmRepo[F],
  roleRepo: RoleRepo[F],
  realmRepo: RealmRepo[F],
  gameRepo: GameRepo[F],
  seasonRepo: SeasonRepo[F],
  userRepo: UserRepo[F],
  xa: HikariTransactor[F]
) {

  def exportArchive: Flow[F, Array[Byte]] = {
    val bos                           = new ByteArrayOutputStream()
    implicit val zos: ZipOutputStream = new ZipOutputStream(bos)
    for {
      algorithms <- algorithmRepo.listAll.transact(xa).attemptSql.adaptError
      _          <- zipData("algorithm.json", algorithms)
      roles      <- roleRepo.listAll.transact(xa).attemptSql.adaptError
      _          <- zipData("role.json", roles)
      realms     <- realmRepo.listAll.transact(xa).attemptSql.adaptError
      _          <- zipData("realm.json", roles)
      _          <- zipSeasons(realms)
      _          <- zipUsers(realms)
      eloPoints  <- gameRepo.listEloPoints(PageInfo(0, 1_000_000)).transact(xa).attemptSql.adaptError
      _          <- zipData("eloPoints.json", eloPoints.items)
      histories  <- gameRepo.listAllGameHistory(PageInfo(0, 1_000_000)).transact(xa).attemptSql.adaptError
      _          <- zipData("gameHistory.json", histories.items)
      _          <- Flow.effect(Sync[F].delay(zos.closeEntry()))
    } yield bos.toByteArray
  }

  private def zipUsers(realms: List[Realm])(implicit zos: ZipOutputStream): Flow[F, Unit] =
    realms
      .map(r => userRepo.listAll(r.name).transact(xa).attemptSql.adaptError)
      .zipWithIndex
      .map(data => data._1.flatMap(users => zipData(s"users_${data._2}.json", users)))
      .sequence_

  private def zipSeasons(realms: List[Realm])(implicit zos: ZipOutputStream): Flow[F, Unit] = {
    val findSeasons =
      seasonRepo.listAll(realms = NonEmptyList.fromList(realms.map(_.name))).transact(xa).attemptSql.adaptError
    for {
      seasons <- findSeasons
      _       <- zipData("seasons.json", seasons)
    } yield ()
  }

  private def zipData[T](name: String, data: List[T])(implicit
    zos: ZipOutputStream,
    encoder: Encoder[T]
  ): Flow[F, Unit] = {
    val syncEffect = Sync[F].delay {
      val e     = new ZipEntry(name)
      val bytes = data.asJson.noSpaces.getBytes
      zos.putNextEntry(e)
      zos.write(bytes)
      zos.closeEntry()
    }
    Flow.effect(syncEffect)
  }
}

object DumpDataManager {
  lazy implicit val AlgorithmEncoder: Encoder[Algorithm]     = deriveEncoder[Algorithm]
  lazy implicit val RoleEncoder: Encoder[Role]               = deriveEncoder[Role]
  lazy implicit val RealmEncoder: Encoder[Realm]             = deriveEncoder[Realm]
  lazy implicit val SeasonEncoder: Encoder[Season]           = deriveEncoder[Season]
  lazy implicit val UsersEncoder: Encoder[User]              = deriveEncoder[User]
  lazy implicit val GameHistoryEncoder: Encoder[GameHistory] = deriveEncoder[GameHistory]
  lazy implicit val EloPointsEncoder: Encoder[EloPoints]     = deriveEncoder[EloPoints]
}
