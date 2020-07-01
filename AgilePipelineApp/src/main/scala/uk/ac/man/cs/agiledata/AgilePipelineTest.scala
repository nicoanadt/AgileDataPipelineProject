package uk.ac.man.cs.agiledata

import org.apache.spark.sql
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.Column
import uk.ac.man.cs.agiledata.cfg._

object AgilePipelineTest {

  def main(args: Array[String]): Unit = {

    if(args.isEmpty) {
      println("Argument is empty")
      return
    }
    val workflowID = args(0)


    // GET Configuration from Database
    val configFromDB = new ConfigDB().getConfiguration(workflowID)

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
    val schemaConfiguration = new WFConfigSchema(configFromDB)
    val schemaEngine = new AgilePipelineSchema()
    val sourceSchema = schemaEngine.getStruct( schemaConfiguration.getConfigSchema() )

    // 2. SOURCE
    val sourceConfiguration = new WFConfigSource(configFromDB)
    val srcConfigMap = sourceConfiguration.getConfigSource().getMap()

    // 3. OPS
    val opsConfiguration = new WFConfigOps(configFromDB)
    val opsEngine = new AgilePipelineOps()

    // 4. TARGET
    val targetConfiguration = new WFConfigTarget(configFromDB)
    val tgtConfigMap = targetConfiguration.getConfigTarget().getMap()


    // Read from kafka stream ------------------------------------------------------
    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", srcConfigMap("broker"))
      .option("subscribe", srcConfigMap("topic"))
      .option("failOnDataLoss","false")
      .option("startingOffsets", srcConfigMap("startingOffsets"))
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
          opsEngine.Filter(opsResult,opsRow.getOpsArrParams()(0))

        case opsRow: ConfigOpsStrTuples if opsRow.getOpsName() == "Rename" =>
          opsEngine.Rename(opsResult, opsRow.getOpsTuplesParams())

        case opsRow: ConfigOpsArr if opsRow.getOpsName() == "Drop" =>
          opsEngine.Drop(opsResult,opsRow.getOpsArrParams())

        case opsRow: ConfigOpsStrTuples if opsRow.getOpsName() == "Add" =>
          opsEngine.Add(opsResult,opsRow.getOpsTuplesParams())

        case opsRow: ConfigOpsAgg if opsRow.getOpsName() == "Agg" =>
          opsEngine.Aggregate(opsResult,opsRow)

        case opsRow: ConfigOpsMap if opsRow.getOpsName() == "Join" =>
          opsEngine.Join(opsResult, opsRow, spark)

      }
    }


    // CONVERT BACK TO JSON STRING
    val dfI = opsResult.selectExpr("CAST(null AS STRING) AS key", "to_json(struct(*)) AS value")

    // START STREAMING to output
    val query = dfI
      .writeStream // use `write` for batch, like DataFrame
      .format("kafka")
      .option("kafka.bootstrap.servers", tgtConfigMap("broker"))
      .option("topic", tgtConfigMap("topic"))
      .option("checkpointLocation", tgtConfigMap("checkpointLocation"))
      .start()

    // NEED to wait termination signal before exiting the app
    query.awaitTermination()
  }

}


