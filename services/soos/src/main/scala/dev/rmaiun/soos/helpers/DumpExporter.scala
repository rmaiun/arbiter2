package dev.rmaiun.soos.helpers

import cats.Monad
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import io.chrisdavenport.log4cats.Logger

case class DumpExporter[F[_]: Monad: Logger](cfg: Config) {

  def sendDump(zipData: Array[Byte]): Flow[F, Unit] = ???
}
