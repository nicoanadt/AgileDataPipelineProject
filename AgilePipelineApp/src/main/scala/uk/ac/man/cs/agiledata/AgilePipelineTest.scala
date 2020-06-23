package uk.ac.man.cs.agiledata

import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.Column

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

    val broker = "kafka:9090"

    // Can have multiple topics separated by comma
    val topic_source = "trafficTopic1"
    val topic_target = "trafficTopicAvg"

    // Read from kafka stream
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", broker)
      .option("subscribe", topic_source)
      .option("failOnDataLoss","false")
      .option("startingOffsets", "earliest")
      .load()

    // EXTRACT KEY and VALUE only while casting to string
    val dfA = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as[(String, String)]

    // SCHEMA Definition
    val rawpvrSchema = new StructType()
      .add("site_id",StringType, true)
      .add("date",StringType, true)
      .add("lane",IntegerType, true)
      .add("lane_name",StringType, true)
      .add("direction",IntegerType, true)
      .add("direction_name",StringType, true)
      .add("reverse",IntegerType, true)
      .add("class_scheme",IntegerType, true)
      .add("class",IntegerType, true)
      .add("class_name",StringType, true)
      .add("length",FloatType, true)
      .add("headway",FloatType, true)
      .add("gap",FloatType, true)
      .add("speed",FloatType, true)
      .add("weight",FloatType, true)
      .add("vehicle_id",StringType, true)
      .add("flags",IntegerType, true)
      .add("flag_text",StringType, true)
      .add("num_axles",IntegerType, true)
      .add("axle_weights",StringType, true)
      .add("axle_spacings",StringType, true)




    // CONVERT JSON STRING in VALUE to JSON STRUCTURE using SCHEMA
    val dfB = dfA.withColumn("value", from_json(col("value"), rawpvrSchema))

    // EXPLODE JSON STRUCTURE in VALUE to its own columns
    val dfC = dfB.select("value.*")

    // DO SOME OPERATION HERE ...

    /// AGILE PIPELINE

    var WFConfigs = new WFConfig()
    var Ops = new AgilePipelineOps()

    var opsResult: sql.DataFrame = dfC

    /**
     * Cycle through operations to create the pipeline based on configuration
     */
    for (config <- WFConfigs.getWFConfigs()) {

      opsResult = config match {
        case config: WFConfigOpsArr if config.getOpsName() == "Filter" =>
          print("filter",false)
          Ops.Filter(opsResult,config.getOpsArrParams()(0))

        case config: WFConfigOpsArr if config.getOpsName() == "Rename" =>
          print("rename",false)
          Ops.Rename(opsResult, config.getOpsArrParams())

        case config: WFConfigOpsArr if config.getOpsName() == "Drop" =>
          print("Drop",false)
          Ops.Drop(opsResult,config.getOpsArrParams())

        case config: WFConfigOpsColTuples if config.getOpsName() == "Add" =>
          print("add",false)
          Ops.Add(opsResult,config.getOpsTuplesParams())

        case config: WFConfigOpsAgg if config.getOpsName() == "Agg" =>
          print("Agg",false)
          Ops.Aggregate(opsResult,config)

      }
    }


    // CONVERT BACK TO JSON STRING
    val dfI = opsResult.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

    // START STREAMING to output
    val query = dfI
      .writeStream // use `write` for batch, like DataFrame
      .format("kafka")
      .option("kafka.bootstrap.servers", broker)
      .option("topic", topic_target)
      .option("checkpointLocation", "/tmp/apps/checkpoint")
      .start()

    // NEED to wait termination signal before exiting the app
    query.awaitTermination()
  }




}


