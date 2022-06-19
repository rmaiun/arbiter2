package dev.rmaiun.mabel.db.queries

import doobie.Meta
import doobie.implicits.javasql._

import java.sql.Timestamp
import java.time.{ ZoneOffset, ZonedDateTime }

trait CustomMeta {
  implicit val metaInstance: Meta[ZonedDateTime] = Meta[Timestamp]
    .imap(ts => ZonedDateTime.ofInstant(ts.toInstant, ZoneOffset.UTC))(zdt => Timestamp.from(zdt.toInstant))
}
