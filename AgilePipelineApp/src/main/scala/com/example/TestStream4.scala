package com.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, col, from_json, to_timestamp, upper, window}
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

object TestStream4 {

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

    val example =
      """select upper(class_name) as class_name_up,
         direction_name as compass_bearing, avg(speed) as avg_speed from rawpvr where headway>=0
         group by upper(class_name), direction_name"""

    /// 1 FILTER
    val dfD = dfC.filter("headway >= 0")

    /// 2 RENAME
    val dfE = dfD.withColumnRenamed("direction_name","compass_bearing")

    /// 3 REMOVE COL
    val dfF = dfE.drop("site_id")
//      .drop("date")
      .drop("lane")
      .drop("lane_name")
      .drop("direction")
//      .drop("direction_name")
      .drop("reverse")
      .drop("class_scheme")
      .drop("class")
//      .drop("class_name")
      .drop("length")
      .drop("headway")
      .drop("gap")
//      .drop("speed")
      .drop("weight")
      .drop("vehicle_id")
      .drop("flags")
      .drop("flag_text")
      .drop("num_axles")
      .drop("axle_weights")
      .drop("axle_spacings")


    /// 4 ADD COL
    val dfG = dfF.withColumn("class_name_up", upper(col("class_name")))
      .withColumn("date", to_timestamp(col("date")))

    /// 5 AGGREGATION
     val dfH = dfG
      .withWatermark("date", "10 minutes")
      .groupBy(
        window($"date", "10 minutes", "5 minutes"),
        $"class_name_up",
        $"compass_bearing"
      )
//      .avg("speed")
       .agg(avg("speed").as("avg_spd"))

    ///6 JOIN
    // val lkp = spark.read.format("csv").option("header", "true").load("file:///data/spark/data/sensorlookup.csv")
    // val b = a.join(lkp, $"site_id" === $"siteident", "left_outer")

    // CONVERT BACK TO JSON STRING
    val dfI = dfH.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

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
