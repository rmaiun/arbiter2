package dev.rmaiun.mongocore.codec

trait MongoCodec[T] {
  def decode()
}
