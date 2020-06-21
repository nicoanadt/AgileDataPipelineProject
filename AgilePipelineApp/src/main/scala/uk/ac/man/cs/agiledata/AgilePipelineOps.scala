package uk.ac.man.cs.agiledata

import org.apache.spark.sql
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

/**
 * Execute the data pipeline based on the parameters given
 */
class AgilePipelineOps {
  def Filter(dfInput : sql.DataFrame, conditions: String ): sql.DataFrame = {
    return dfInput.filter(conditions)
  }

  def Rename(dfInput : sql.DataFrame, existingName: String, newName:String): sql.DataFrame = {
    return dfInput.withColumnRenamed(existingName,newName)
  }

  def Drop(dfInput : sql.DataFrame, colNames: Array[String]): sql.DataFrame = {
    var dfTemp1 = dfInput
    for (colName <- colNames) {
      dfTemp1 = dfTemp1.drop(colName)
    }
    return dfTemp1
  }

  def Add(dfInput : sql.DataFrame, paramAddTuples: Array[(String,Column)]): sql.DataFrame = {
    var dfTemp1 = dfInput
    for (tuple <- paramAddTuples) {
      dfTemp1 = dfTemp1.withColumn(tuple._1, tuple._2)
    }
    return dfTemp1
  }

  def Aggregate(dfInput : sql.DataFrame, WMCol: String, WMThreshold: String,
                WindowCol: String, WindowDur: String,
                GroupByCols: Array[String],
                AggType: String, aggCol: String
               ): sql.DataFrame = {

    val dfTemp1 = dfInput
      .withWatermark(WMCol, WMThreshold)

    val dfTemp2 = dfTemp1
      .groupBy(
        window(col(WindowCol), WindowDur): Column,
        col(GroupByCols(0)),
        col(GroupByCols(1))
      )

    if (AggType == "avg") {
      return dfTemp2.avg(aggCol)
    }
    else return dfInput


//    val dfH = dfG
//      .withWatermark("date", "10 minutes")
//      .groupBy(
//        window($"date", "10 minutes", "5 minutes"),
//        $"class_name_up",
//        $"compass_bearing"
//      )
//      .avg("speed")
  }



}
