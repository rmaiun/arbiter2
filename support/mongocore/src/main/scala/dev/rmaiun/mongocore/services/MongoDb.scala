package dev.rmaiun.mongocore.services

import dev.rmaiun.mongocore.config.MongoCfg
import org.bson.codecs.configuration.CodecRegistries.fromRegistries
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.{ MongoClient, MongoDatabase }

object MongoDb {

  def init(cfg: MongoCfg): MongoDatabase = {
    val mongoClient: MongoClient = MongoClient(cfg.mongoUri)
    val codecRegistry            = fromRegistries(cfg.codecRegistry, DEFAULT_CODEC_REGISTRY)
    mongoClient
      .getDatabase(cfg.db)
      .withCodecRegistry(codecRegistry)
  }
}
