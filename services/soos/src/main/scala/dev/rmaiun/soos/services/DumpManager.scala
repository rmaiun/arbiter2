package dev.rmaiun.soos.services

import cats.{Applicative, Monad}
import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.foldable._
import dev.rmaiun.errorhandling.ThrowableOps._
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.db.entities._
import dev.rmaiun.soos.repositories._
import dev.rmaiun.soos.services.DumpManager._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.circe.Encoder
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import java.io.ByteArrayOutputStream
import java.util.zip.{ZipEntry, ZipOutputStream}
import scala.util.Try

case class DumpManager[F[_]: Monad: Sync](
  algorithmRepo: AlgorithmRepo[F],
  roleRepo: RoleRepo[F],
  realmRepo: RealmRepo[F],
  gameRepo: GameRepo[F],
  seasonRepo: SeasonRepo[F],
  userRepo: UserRepo[F],
  xa: HikariTransactor[F]
) {

  def exportArchive: Flow[F, Array[Byte]] = {
    val bos                           = new ByteArrayOutputStream();
    implicit val zos: ZipOutputStream = new ZipOutputStream(bos)
    for {
      algorithms <- algorithmRepo.listAll.transact(xa).attemptSql.adaptError
      _          <- zipData("algorithms.json", algorithms)
      roles      <- roleRepo.listAll.transact(xa).attemptSql.adaptError
      _          <- zipData("roles.json", roles)
      realms     <- realmRepo.listAll.transact(xa).attemptSql.adaptError
      _          <- zipData("realms.json", roles)
      _          <- zipSeasons(realms)
      _          <- zipUsers(realms)
      _          <- Flow.effect(Sync[F].delay(zos.closeEntry()))
    } yield bos.toByteArray
  }

  private def zipGameHistories() = ???
  private def zipGamePoints()    = ???

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
    encoder: Encoder[List[T]]
  ): Flow[F, Unit] =
    Flow
      .fromEither(Try {
        val e     = new ZipEntry(name)
        val bytes = encoder(data).toString().getBytes
        zos.putNextEntry(e)
        zos.write(bytes)
        zos.closeEntry()
      }.toEither)(implicitly[Applicative[F]])
      .map(_ => ())
}

object DumpManager {
  implicit val AlgorithmEncoder: Encoder[List[Algorithm]]     = deriveEncoder[List[Algorithm]]
  implicit val RoleEncoder: Encoder[List[Role]]               = deriveEncoder[List[Role]]
  implicit val RealmEncoder: Encoder[List[Realm]]             = deriveEncoder[List[Realm]]
  implicit val SeasonEncoder: Encoder[List[Season]]           = deriveEncoder[List[Season]]
  implicit val UsersEncoder: Encoder[List[User]]              = deriveEncoder[List[User]]
  implicit val GameHistoryEncoder: Encoder[List[GameHistory]] = deriveEncoder[List[GameHistory]]
  implicit val EloPointsEncoder: Encoder[List[EloPoints]]     = deriveEncoder[List[EloPoints]]

}
