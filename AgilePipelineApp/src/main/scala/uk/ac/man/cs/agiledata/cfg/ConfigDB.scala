package uk.ac.man.cs.agiledata.cfg

import org.mongodb.scala._
import org.mongodb.scala.model.Projections._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

// CASE CLASSES FOR MONGODB MACROS

case class WFConfig(
                     config: Config,
                     workflow: Workflow,
                     execution: Execution
                   )

case class Execution(app_name: String)

case class Config(name: String, desc: String, created_by: String, date_created: String, date_modified: String)

case class Schema(name: String, datatype: String)

case class Source(
                   source_type: String,
                   broker: String,
                   topic: String,
                   startingOffsets: String,
                   schema: List[Schema]
                 )

case class Ops(
                ops_type: String,
                params_filter: List[opsParamFilter],
                params_rename: List[opsParamRename],
                params_drop: List[String],
                params_add: List[opsParamAdd],
                params_agg: opsParamAgg
              )

case class opsParam(
                     expr: String,
                     new_name: String,
                     old_name: String,
                     drop_col: String,
                     WatermarkColumn: String,
                     WatermarkDelayThreshold: String,
                     WindowTimeColumn: String,
                     WindowDuration: String
                   )

case class opsParamFilter(
                           expr: String
                         )

case class opsParamRename(
                           old_name: String,
                           new_name: String
                         )

case class opsParamAdd(
                        new_name: String,
                        expr: String
                      )

case class opsParamAgg(
                        WatermarkColumn: String,
                        WatermarkDelayThreshold: String,
                        WindowTimeColumn: String,
                        WindowDuration: String,
                        groupByCols: List[String],
                        aggCols: List[String]
                      )

case class Target(
                   target_type: String,
                   broker: String,
                   topic: String,
                   checkpointLocation: String
                 )

case class Workflow( source: Source, ops: List[Ops], target: Target )

// END OF CASE CLASSES FOR MONGODB MACROS

object DB {
  import org.bson.codecs.configuration.CodecRegistries
  import org.bson.codecs.configuration.CodecRegistries._
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
  import org.mongodb.scala.bson.codecs.Macros._
  import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

  private val customCodecs = fromProviders(
    createCodecProviderIgnoreNone(classOf[WFConfig]),
    createCodecProviderIgnoreNone(classOf[Config]),
    createCodecProviderIgnoreNone(classOf[Schema]),
    createCodecProviderIgnoreNone(classOf[Source]),
    createCodecProviderIgnoreNone(classOf[Ops]),
    createCodecProviderIgnoreNone(classOf[Target]),
    createCodecProviderIgnoreNone(classOf[Workflow]),
    createCodecProviderIgnoreNone(classOf[opsParamFilter]),
    createCodecProviderIgnoreNone(classOf[opsParamRename]),
    createCodecProviderIgnoreNone(classOf[opsParamAdd]),
    createCodecProviderIgnoreNone(classOf[opsParamAgg]),
    createCodecProviderIgnoreNone(classOf[Execution])
  )
  private val codecRegistry = fromRegistries(customCodecs,DEFAULT_CODEC_REGISTRY)

  val uri: String = "mongodb://datapipeline:bigdata2020@mongodb_container:27017/datapipeline?retryWrites=true&w=majority"
  System.setProperty("org.mongodb.async.type", "netty")
  val client: MongoClient = MongoClient(uri)
  private val database: MongoDatabase = client.getDatabase("datapipeline")
    .withCodecRegistry(codecRegistry)

  val configCollection: MongoCollection[WFConfig] = database.getCollection("config")

  val allConfigs = Await.result(configCollection.find().toFuture(), Duration.Inf)

  allConfigs(0).workflow.ops(0).ops_type
  allConfigs(0).workflow.ops(0).params_filter(0).expr

}

//class ConfigDB {
//  def getConnection(): MongoDatabase = {
//    val uri: String = "mongodb://datapipeline:bigdata2020@mongodb_container:27017/datapipeline?retryWrites=true&w=majority"
//    System.setProperty("org.mongodb.async.type", "netty")
//    val client: MongoClient = MongoClient(uri)
//    val db: MongoDatabase = client.getDatabase("datapipeline")
//
//    val coll = db.getCollection("config")
//
////    val e = coll.find(Document("id" -> "WFA")).map(dbo => dbo.get("config"))
////    val e = coll.find(Document("config.name" -> "Configuration A")).map(dbo => dbo.get("config"))
////    val r = Await.result(e.toFuture(), Duration.Inf)
//
//    val e = coll.find(Document("config.name" -> "Configuration A"))
//      .projection(
//        fields(
//          include("config.name"),
//          include("config.desc")
//          , excludeId()))
//    val r = Await.result(e.toFuture(), Duration.Inf)
//
//    val bsonStr =  r.toList(0).toMap.get("config").get.toString
//    BsonDocument
//    val a = r.toArray.map(x => x.get.toString).mkString
//    val a = r.toArray.map(x => x.get.asString())
//
////    parse(jsonString).values.asInstanceOf[Map[String, Any]]
//
//    return db
//  }
}
