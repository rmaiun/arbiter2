package dev.rmaiun.mongocore.config

import org.bson.codecs.configuration.CodecRegistry
case class MongoCfg(mongoUri: String, db:String, codecRegistry: CodecRegistry)
