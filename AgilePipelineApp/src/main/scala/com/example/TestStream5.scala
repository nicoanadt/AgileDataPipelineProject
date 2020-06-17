package com.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

object TestStream5 {

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
    val topic_target = "trafficTopic2"

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

    // 0 REGISTER TABLE
    dfC.createOrReplaceTempView("rawpvr")

    /// 1 FILTER
    val dfD = spark.sql("select * from rawpvr where headway>=0")

    val example = """select upper(k1) as k1, k2 as k2new, k3 ,k5, sum(v1) as sum_v1 from rawpvr where headway>=0
      group by upper(k1), k2, k3, k5"""

    val example2 =
      """select upper(class_name) as class_name_up,
         direction_name as compass_bearing, avg(speed) as avg_speed from rawpvr where headway>=0
         group by upper(class_name), direction_name"""

    /// 2 RENAME
    // val dfE = spark.sql("select *,a as b from rawpvr")

    /// 3 REMOVE COL
    //val dfF = spark.sql("select *,a as b from rawpvr")

    /// 4 ADD COL
    // val dfG = dfF.withColumn("k1added", upper(col("k1")))

    /// 5 AGGREGATION
    // val dfH = goalsDF
    //  .withWatermark("timestamp", "10 minutes")
    //  .groupBy("name")
    //  .agg(sum("goals"))

    // CONVERT BACK TO JSON STRING
    val dfH = dfD.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

    // START STREAMING to output
    val query = dfH
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
