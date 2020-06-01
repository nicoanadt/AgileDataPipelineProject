package com.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{lit, schema_of_json, from_json, explode, col}
import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType, FloatType};
import org.apache.spark.sql.Column

object TestStream3 {

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
    val topic_source = "simple1"
    val topic_target = "simple2"

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
      .add("k1",StringType, true)
      .add("k2",StringType, true)
      .add("k3",StringType, true)
      .add("k4",IntegerType, true)
      .add("k5",FloatType, true)

    // CONVERT JSON STRING in VALUE to JSON STRUCTURE using SCHEMA
    val dfB = dfA.withColumn("value", from_json(col("value"), rawpvrSchema))

    // EXPLODE JSON STRUCTURE in VALUE to its own columns
    val dfC = dfB.select("value.*")

    // DO SOME OPERATION HERE ...
    val dfD = dfC.filter("k4 == 8")

    // CONVERT BACK TO JSON STRING
    val dfE = dfD.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

    // START STREAMING to output
    val query = dfE
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
