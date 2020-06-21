package uk.ac.man.cs.agiledata
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.functions._

/**
 *
 */
class WFConfig {
  var ops = new ArrayBuffer[WFOps]()

  ops += new WFOpsArr("Filter", Array("headway >= 0"))
  ops += new WFOpsArr("Rename", Array("direction_name","compass_bearing"))
  ops += new WFOpsArr("Drop", Array(
    "site_id", /*"date",*/ "lane", "lane_name", "direction",
    /*"direction_name",*/ "reverse", "class_scheme", "class", /*"class_name",*/ "length", "headway", "gap",
    /*"speed",*/ "weight","vehicle_id", "flags", "flag_text", "num_axles", "axle_weights", "axle_spacings")
  )
  ops += new WFOpsTuples("Add",
      Array(
        ("class_name_up", upper(col("class_name")) ),
        ("date", to_timestamp(col("date")) )
      )
    )
//  ops += new WFOpsAdd("Add",
//    Map(
//    "class_name_up" -> upper(col("test")),
//    "date" -> to_timestamp(col("date"))
//    )
//  )



  def getWFConfig() : Array[WFOps] ={
    return ops.toArray
  }
}
