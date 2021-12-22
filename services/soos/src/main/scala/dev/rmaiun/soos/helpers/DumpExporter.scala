package dev.rmaiun.soos.helpers

import cats.effect.Sync
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.{ Flow, MonadThrowable }
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import io.chrisdavenport.log4cats.Logger

import java.io.ByteArrayInputStream

case class DumpExporter[F[_]: MonadThrowable: Logger: Sync](dataManager: DumpDataManager[F], cfg: Config) {

  def exportDump(): Flow[F, Unit] =
    for {
      zipBytes <- dataManager.exportArchive
      _        <- transferDump(zipBytes)
    } yield ()

  private def transferDump(zipData: Array[Byte]): Flow[F, Unit] = {
    val delayed = Sync[F].delay {
      val config = DbxRequestConfig.newBuilder("dropbox/arbiter2").build
      val client = new DbxClientV2(
        config,
        new DbxCredential(
          "",
          1L,
          cfg.archive.token,
          cfg.archive.key1,
          cfg.archive.key2
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
