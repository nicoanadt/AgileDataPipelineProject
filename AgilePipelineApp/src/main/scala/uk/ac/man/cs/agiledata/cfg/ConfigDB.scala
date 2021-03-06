package uk.ac.man.cs.agiledata.cfg

import org.mongodb.scala._
import org.mongodb.scala.model.Projections._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * The following case classes are used for MongoDB Macros
 * The JSON file is loaded and instantiated to WFConfig object in Scala
 *
 */

case class WFConfig(
                     id: String,
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
                ops_order: String,
                ops_type: String,
                params_filter: List[opsParamFilter],
                params_rename: List[opsParamRename],
                params_drop: List[String],
                params_add: List[opsParamAdd],
                params_agg: opsParamAgg,
                params_join: opsParamJoin
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

case class opsParamJoin(
                         join_type: String,
                         join_to_csv_dataset: String,
                         join_expr: String
                      )

case class Target(
                   target_type: String,
                   broker: String,
                   topic: String,
                   checkpointLocation: String
                 )

case class Workflow( source: Source, ops: List[Ops], target: Target )

/**
 * END OF CASE CLASSES FOR MONGODB MACROS
 */

/**
 * This class is responsible to connect to the MongoDB metastore database
 */
class ConfigDB {

  /**
   * This function connects to a database and return a single configuration based on the supplied wf unique id
   *
   * @param workflowName Unique identifier of the workflow config
   * @param connString Connection string of the MongoDB
   * @param dbName Database name of MongoDB
   * @param collName MongoDB collection name
   * @return One single WFConfig object consists of the workflow configuration
   */
  def getConfiguration(workflowName: String, connString: String, dbName: String, collName:String): WFConfig ={

    import org.bson.codecs.configuration.CodecRegistries
    import org.bson.codecs.configuration.CodecRegistries._
    import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
    import org.mongodb.scala.bson.codecs.Macros._
    import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

    // Create custom codecs
    val customCodecs =
    fromProviders(
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
      createCodecProviderIgnoreNone(classOf[Execution]),
      createCodecProviderIgnoreNone(classOf[opsParamJoin])
    )

    // Create codec registry based on custom codecs, merge with default registry
    val codecRegistry =
    fromRegistries(customCodecs, DEFAULT_CODEC_REGISTRY)

    // Connect to mongodb database
    //val uri: String = "mongodb://datapipeline:bigdata2020@mongodb_container:27017/datapipeline?retryWrites=true&w=majority"
    //val uri: String = "mongodb://AdalineFramework:bigdata2020@mongodb_container:27017/AdalineFramework?retryWrites=true&w=majority"
    System.setProperty("org.mongodb.async.type", "netty")
    val client: MongoClient = MongoClient(connString)
    val database: MongoDatabase = client.getDatabase(dbName).withCodecRegistry(codecRegistry)

    // Get collection
    val configCollection: MongoCollection[WFConfig] = database.getCollection(collName)

    // Query collection based on config.name, wait for result
    val allConfigs = Await.result(configCollection.find(Document("id" -> workflowName)).toFuture(), Duration.Inf)
    client.close()

    // Return first result
    return allConfigs(0)
  }
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
//}
