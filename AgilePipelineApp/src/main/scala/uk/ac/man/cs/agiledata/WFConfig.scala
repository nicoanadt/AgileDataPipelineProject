package uk.ac.man.cs.agiledata

import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.functions._

/**
 *
 */
class WFConfig {
  var ops = new ArrayBuffer[WFConfigOps]()

  ops += new WFConfigOpsArr("Filter", Array("headway >= 0"))
  ops += new WFConfigOpsArr("Rename", Array("direction_name","compass_bearing"))
  ops += new WFConfigOpsArr("Drop",
    Array(
      "site_id", /*"date",*/ "lane", "lane_name", "direction",
      /*"direction_name",*/ "reverse", "class_scheme", "class", /*"class_name",*/ "length", "headway", "gap",
      /*"speed",*/ "weight","vehicle_id", "flags", "flag_text", "num_axles", "axle_weights", "axle_spacings"
    )
  )
  ops += new WFConfigOpsColTuples("Add",
      Array(
        ("class_name_up", expr("upper(class_name)") ),
        ("date", expr("to_timestamp(date)") )
      )
    )

  ops += new WFConfigOpsAgg("Agg",
    Map(
      ("WatermarkColumn" -> "date"),
      ("WatermarkDelayThreshold" -> "10 minutes"),
      ("WindowTimeColumn" -> "date"),
      ("WindowDuration" -> "10 minutes")
//      ("WindowSlideDuration" -> null)
    ),
    Map(
      ("groupByCols" -> Array("class_name_up","compass_bearing")),
      ("aggCols" -> Array("avg(speed) as avg_speed"))
    )
  )



  def getWFConfigs() : Array[WFConfigOps] ={
    return ops.toArray
  }
}
