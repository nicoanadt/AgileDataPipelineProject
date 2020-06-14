package com.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, from_json, upper}
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

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

    /// 1 FILTER
    val dfD = dfC.filter("k2 == 'v2'")

    /// 2 RENAME
    // val dfE = df.withColumnRenamed("dob","DateOfBirth")
    val dfE = dfD.withColumnRenamed("k2","k2new")

    /// 3 REMOVE COL
    val dfF = dfE.drop("k3")

    /// 4 ADD COL
     val dfG = dfF.withColumn("k1added", upper(col("k1")))

    /// 5 AGGREGATION
//    val dfH = dfG
//      .groupBy("name")
//      .agg(sum("goals"))

    // CONVERT BACK TO JSON STRING
    val dfH = dfG.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

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
