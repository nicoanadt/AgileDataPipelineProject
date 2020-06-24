package uk.ac.man.cs.agiledata

import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.Column
import uk.ac.man.cs.agiledata.cfg.{ConfigOpsAgg, ConfigOpsArr, ConfigOpsStrTuples, WFConfigOps, WFConfigSchema, WFConfigSource, WFConfigTarget}

object AgilePipelineTest {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("spark://sparkmst:7077")
      .appName("AgilePipeline")
      //.config("spark.some.config.option", "some-value")
      .getOrCreate()

    // For implicit conversions like converting RDDs to DataFrames
    import spark.implicits._

    // GET CONFIGURATIONS ------------------------------------------------------
    // 1. SCHEMA
    val schemaConfiguration = new WFConfigSchema()
    val schemaEngine = new AgilePipelineSchema()
    val sourceSchema = schemaEngine.getStruct( schemaConfiguration.getConfigSchema() )

    // 2. SOURCE
    val sourceConfiguration = new WFConfigSource()
    val src_broker = sourceConfiguration.getConfigSource().getMap()("broker")
    val src_topic = sourceConfiguration.getConfigSource().getMap()("topic")
    val src_startingOffsets = sourceConfiguration.getConfigSource().getMap()("startingOffsets")

    // 3. OPS
    val opsConfiguration = new WFConfigOps()
    val opsEngine = new AgilePipelineOps()

    // 4. TARGET
    val targetConfiguration = new WFConfigTarget()
    val tgt_broker = targetConfiguration.getConfigTarget().getMap()("broker")
    val tgt_topic = targetConfiguration.getConfigTarget().getMap()("topic")
    val tgt_checkpointLocation = targetConfiguration.getConfigTarget().getMap()("checkpointLocation")


    // Read from kafka stream ------------------------------------------------------
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", src_broker)
      .option("subscribe", src_topic)
      .option("failOnDataLoss","false")
      .option("startingOffsets", src_startingOffsets)
      .load()

    // EXTRACT KEY and VALUE only while casting to string
    val dfA = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as[(String, String)]


    // CONVERT JSON STRING in VALUE to JSON STRUCTURE using SCHEMA
    val dfB = dfA.withColumn("value", from_json(col("value"), sourceSchema))

    // EXPLODE JSON STRUCTURE in VALUE to its own columns
    val dfC = dfB.select("value.*")

    // DO SOME OPERATION HERE
    // AGILE PIPELINE

    // Assign opsResult as preparation of loop
    var opsResult: sql.DataFrame = dfC

    /**
     * Cycle through operations to create the pipeline based on configuration
     */
    for (opsRow <- opsConfiguration.getConfigOps()) {

      opsResult = opsRow match {
        case opsRow: ConfigOpsArr if opsRow.getOpsName() == "Filter" =>
          print("filter",false)
          opsEngine.Filter(opsResult,opsRow.getOpsArrParams()(0))

        case opsRow: ConfigOpsArr if opsRow.getOpsName() == "Rename" =>
          print("rename",false)
          opsEngine.Rename(opsResult, opsRow.getOpsArrParams())

        case opsRow: ConfigOpsArr if opsRow.getOpsName() == "Drop" =>
          print("Drop",false)
          opsEngine.Drop(opsResult,opsRow.getOpsArrParams())

        case opsRow: ConfigOpsStrTuples if opsRow.getOpsName() == "Add" =>
          print("add",false)
          opsEngine.Add(opsResult,opsRow.getOpsTuplesParams())

        case opsRow: ConfigOpsAgg if opsRow.getOpsName() == "Agg" =>
          print("Agg",false)
          opsEngine.Aggregate(opsResult,opsRow)

      }
    }


    // CONVERT BACK TO JSON STRING
    val dfI = opsResult.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

    // START STREAMING to output
    val query = dfI
      .writeStream // use `write` for batch, like DataFrame
      .format("kafka")
      .option("kafka.bootstrap.servers", tgt_broker)
      .option("topic", tgt_topic)
      .option("checkpointLocation", tgt_checkpointLocation)
      .start()

    // NEED to wait termination signal before exiting the app
    query.awaitTermination()
  }

}


