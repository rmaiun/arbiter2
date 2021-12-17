package dev.rmaiun.soos.services

import cats.Monad
import cats.effect.Sync
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.repositories._

case class DumpManager[F[_]: Monad: Sync](
  algorithmRepo: AlgorithmRepo[F],
  roleRepo: RoleRepo[F],
  realmRepo: RealmRepo[F],
  gameRepo: GameRepo[F],
  seasonRepo: SeasonRepo[F],
  userRepo: UserRepo[F]
) {

  def exportArchive: Flow[F, Array[Byte]] = ???
}
