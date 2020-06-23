package uk.ac.man.cs.agiledata

import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.functions._

/**
 *
 */
class WFConfigOps {
  var ops = new ArrayBuffer[ConfigOps]()

  ops += new ConfigOpsArr("Filter", Array("headway >= 0"))
  ops += new ConfigOpsArr("Rename", Array("direction_name","compass_bearing"))
  ops += new ConfigOpsArr("Drop",
    Array(
      "site_id", /*"date",*/ "lane", "lane_name", "direction",
      /*"direction_name",*/ "reverse", "class_scheme", "class", /*"class_name",*/ "length", "headway", "gap",
      /*"speed",*/ "weight","vehicle_id", "flags", "flag_text", "num_axles", "axle_weights", "axle_spacings"
    )
  )
  ops += new ConfigOpsStrTuples("Add",
      Array(
        ("class_name_up", "upper(class_name)"),
        ("date", "to_timestamp(date)")
      )
    )

  ops += new ConfigOpsAgg("Agg",
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


  def getConfigOps() : Array[ConfigOps] ={
    return ops.toArray
  }
}
