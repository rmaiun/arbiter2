package dev.rmaiun.mongocore.services

import cats.effect.{ContextShift, IO}
import dev.rmaiun.mongocore.config.MongoCfg
import dev.rmaiun.tftypes.types.Flow
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.ObjectId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.implicits._

import scala.concurrent.ExecutionContext
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY

case class CustomData(_id:ObjectId, smth: String, data: Subdata)
case class Subdata(x:String, y:Int)
class CustomDataRepo(db: MongoDatabase)(implicit cs:ContextShift[IO]) extends MongoBasicRepo[IO, CustomData]("customData", db)


class CustomRepoTest extends AnyFlatSpec with Matchers {
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val cr: CodecRegistry = fromRegistries(fromProviders(classOf[Subdata], classOf[CustomData]), DEFAULT_CODEC_REGISTRY )
  private val db: MongoDatabase = MongoDb.init(MongoCfg("mongodb://root:password@localhost:27017/cata", "cata", cr))
  val repo = new CustomDataRepo(db)

  "Custom Repo is working" should "custom data is successfully stored to mongo" in  {
    val entity = CustomData(new ObjectId(),"something", Subdata("xxx", 24))

    val result = repo.create(entity).value.unsafeRunSync()
    result.isRight should be(true)
    val resultValue = result.getOrElse(fail("either was not Right!"))
    resultValue._id.toHexString should have length 24

  }

}
