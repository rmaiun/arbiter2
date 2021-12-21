package dev.rmaiun.soos.helpers

import cats.Monad
import com.dropbox.core.{DbxAppInfo, DbxRequestConfig, DbxWebAuth, TokenAccessType}
import dev.rmaiun.flowtypes.Flow
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.soos.helpers.ConfigProvider.Config
import io.chrisdavenport.log4cats.Logger

case class DumpExporter[F[_]: Monad: Logger](cfg: Config) {

  def sendDump(zipData: Array[Byte]): Flow[F, Unit] = {
    val code = ""
    val appInfo = new DbxAppInfo(cfg.archive.key1, cfg.archive.key2)
    val requestConfig  = new DbxRequestConfig("arbiter2")
    val webAuth = new DbxWebAuth(requestConfig, appInfo)
    val webAuthRequest =  DbxWebAuth.newRequestBuilder()
      .withNoRedirect()
      .withTokenAccessType(TokenAccessType.OFFLINE)
      .build()
    val authorizeUrl = webAuth.authorize(webAuthRequest)

    val authFinish = webAuth.finishFromCode(code)

    System.out.println("Authorization complete.")
    System.out.println("- User ID: " + authFinish.getUserId)
    System.out.println("- Access Token: " + authFinish.getAccessToken)
    Flow.unit
  }
}
