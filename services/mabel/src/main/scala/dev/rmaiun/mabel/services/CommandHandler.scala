package dev.rmaiun.mabel.services

import cats.Monad
import com.rabbitmq.client.Delivery
import dev.rmaiun.flowtypes.Flow.Flow
import dev.rmaiun.flowtypes.{ FLog, Flow }
import io.chrisdavenport.log4cats.Logger
import io.circe.Json
import io.circe.parser._

case class CommandHandler[F[_]: Monad: Logger](strategy: ProcessorStrategy[F]) {
  import dev.rmaiun.mabel.dtos.BotRequest._
  def process(record: Delivery): Flow[F, String] = {
    val start = System.currentTimeMillis
    val x     = parse(new String(record.getBody)).getOrElse(Json.fromBoolean(true))
    val value = BotRequestDecoder.decodeJson(x)
    val flow = for {
      json      <- Flow.fromEither(parse(new String(record.getBody)))
      _         <- FLog.info(json.toString())
      input     <- Flow.fromEither(value)
      processor <- strategy.selectProcessor(input.cmd)
      _         <- processor.process(input)
      result    <- processor.process(input)
      _         <- FLog.info(s"Cmd ${input.cmd} (${input.user}) was processed in ${System.currentTimeMillis() - start} ms")
      _         <- FLog.info(result.botResponse.result)
    } yield "OK"
    flow.leftFlatMap(err => Flow.pure(err.getMessage))
  }
}
