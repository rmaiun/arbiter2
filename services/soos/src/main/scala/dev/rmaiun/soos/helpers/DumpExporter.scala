package dev.rmaiun.soos.helpers

import cats.effect.Sync
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.flowtypes.{ FLog, Flow }
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.ByteArrayInputStream

case class DumpExporter[F[_]: MonadThrowable: Sync](zipDataProvider: ZipDataProvider[F], cfg: Config) {
  implicit val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass[F](getClass)

  def exportDump(): Flow[F, Unit] = {
    val exportEffect = for {
      _        <- FLog.info("Dump export started")
      zipBytes <- zipDataProvider.exportArchive
      _        <- transferDump(zipBytes)
      _        <- FLog.info("Dump export successfully finished")
    } yield ()
    exportEffect.leftFlatMap(err => FLog.error(err))
  }

  private def transferDump(zipData: Array[Byte]): Flow[F, Unit] = {
    val delayed = Sync[F].delay {
      val config = DbxRequestConfig.newBuilder("dropbox/arbiter2").build
      val client = new DbxClientV2(
        config,
        new DbxCredential(
          "",
          1L,
          cfg.archive.token,
          cfg.archive.key,
          cfg.archive.secret
        )
      )
      client
        .files()
        .uploadBuilder("/dump.zip")
        .withMode(WriteMode.OVERWRITE)
        .uploadAndFinish(new ByteArrayInputStream(zipData))
    }
    Flow.effect(delayed).map(_ => ())
  }
}
object DumpExporter {
  def apply[F[_]](implicit ev: DumpExporter[F]): DumpExporter[F] = ev

  def impl[F[_]: MonadThrowable: Sync](zipDataProvider: ZipDataProvider[F], cfg: Config): DumpExporter[F] =
    new DumpExporter[F](zipDataProvider, cfg)
}
