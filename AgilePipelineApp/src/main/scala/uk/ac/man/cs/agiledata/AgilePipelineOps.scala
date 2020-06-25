package uk.ac.man.cs.agiledata

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._
import uk.ac.man.cs.agiledata.cfg.ConfigOpsAgg


/**
 * Execute the data pipeline based on the parameters given in WFConfig
 */
class AgilePipelineOps {
  /**
   * FILTER FUNCTION
   *
   *
   * @param dfInput
   * @param conditions
   * @return
   */
  def Filter(dfInput : sql.DataFrame, conditions: String ): sql.DataFrame = {
    return dfInput.filter(conditions)
  }

  /**
   * RENAME COLUMN FUNCTION
   *
   * @param dfInput
   * @param colNames
   * @return
   */
  def Rename(dfInput : sql.DataFrame, paramAddTuples: Array[(String,String)]): sql.DataFrame = {
    var dfTemp1 = dfInput
    for (tuple <- paramAddTuples) {
      dfTemp1 = dfTemp1.withColumnRenamed(tuple._1,tuple._2)
    }
    return dfTemp1
  }

  /**
   * DROP COLUMN FUNCTION
   *
   *
   * @param dfInput
   * @param colNames
   * @return
   */
  def Drop(dfInput : sql.DataFrame, colNames: Array[String]): sql.DataFrame = {
    var dfTemp1 = dfInput
    for (colName <- colNames) {
      dfTemp1 = dfTemp1.drop(colName)
    }
    return dfTemp1
  }

  /**
   * ADD COLUMN FUNCTION
   *
   *
   *
   * @param dfInput
   * @param paramAddTuples
   * @return
   */
  def Add(dfInput : sql.DataFrame, paramAddTuples: Array[(String,String)]): sql.DataFrame = {
    var dfTemp1 = dfInput
    for (tuple <- paramAddTuples) {
      dfTemp1 = dfTemp1.withColumn(tuple._1, expr(tuple._2))
    }
    return dfTemp1
  }

  /**
   * AGGREGATE FUNCTION
   *
   * Example:
   * val dfResult = dfInput.withWatermark("date", "10 minutes")
   * .groupBy(
   *  window($"date", "10 minutes", "5 minutes"),
   *  $"class_name_up",
   *  $"compass_bearing"
   * )
   * .agg(expr("avg(speed) as avg_speed"))
   *
   * @param dfInput
   * @param aggConfig
   * @return
   */
  def Aggregate(dfInput : sql.DataFrame, aggConfig: ConfigOpsAgg): sql.DataFrame = {

    val WatermarkColumn = aggConfig.getOpsMapParams()("WatermarkColumn")
    val WatermarkDelayThreshold = aggConfig.getOpsMapParams()("WatermarkDelayThreshold")
    val WindowTimeColumn = aggConfig.getOpsMapParams()("WindowTimeColumn")
    val WindowDuration = aggConfig.getOpsMapParams()("WindowDuration")
    val WindowSlideDuration = aggConfig.getOpsMapParams().getOrElse("WindowSlideDuration","null")
    val groupByColsArr = aggConfig.getMultiParam()("groupByCols")
    val aggParamArr = aggConfig.getMultiParam()("aggCols")

    // Assign watermark
    val dfTemp1 = dfInput.withWatermark(WatermarkColumn, WatermarkDelayThreshold)

    // Check whether the params include WindowSlideDuration
    val windowColumn = if (WindowSlideDuration != "null") {
      Array(window(col(WindowTimeColumn), WindowDuration, WindowSlideDuration): Column)
    } else {
      Array(window(col(WindowTimeColumn), WindowDuration): Column)
    }

    // Create parameter for group by, consists of an array of column (size n)
    // 1. Windowing parameter (index 0)
    // 2. Group by columns (index 1 .. n-1). Convert from String to Column using expr()
    val paramGroupBy = windowColumn ++ groupByColsArr.map(expr(_))

    // Create grouped dataset using groupBy function
    val dfTemp2 = dfTemp1.groupBy(paramGroupBy:_*)

    // Create parameter for agg, consists of an array of column. Convert from String to Column using expr()
    val paramAgg = aggParamArr.map(expr(_))

    // Function agg has minimum parameter number of 1, thus if more than 1 we accommodate using _*
    val dfResult = if(paramAgg.length > 1) {
      dfTemp2.agg(paramAgg(0), paramAgg.drop(1):_*)
    } else dfTemp2.agg(paramAgg(0))

    return dfResult

  }

}
