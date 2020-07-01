package uk.ac.man.cs.agiledata.cfg

import scala.collection.mutable.ArrayBuffer

/**
 *
 */
class WFConfigOps(configFromDB : WFConfig) {

  // Create empty array for appending pipelines
  var ops = new ArrayBuffer[ConfigOps]()

  // Assemble pipeline from database configuration
  for(x <- configFromDB.workflow.ops) {
    val thisOps = x.ops_type match {
      case "Filter" => new ConfigOpsArr("Filter", x.params_filter.map(x => x.expr).toArray )
      case "Rename" => new ConfigOpsStrTuples("Rename", x.params_rename.map(x => (x.old_name,x.new_name)).toArray)
      case "Drop" => new ConfigOpsArr("Drop", x.params_drop.toArray)
      case "Add" => new ConfigOpsStrTuples("Add", x.params_add.map( x => (x.new_name,x.expr)).toArray)
      case "Agg" => new ConfigOpsAgg("Agg",
          Map(
            ("WatermarkColumn" -> x.params_agg.WatermarkColumn),
            ("WatermarkDelayThreshold" -> x.params_agg.WatermarkDelayThreshold),
            ("WindowTimeColumn" -> x.params_agg.WindowTimeColumn),
            ("WindowDuration" -> x.params_agg.WindowDuration)
          ),
        Map(
          ("groupByCols" -> x.params_agg.groupByCols.toArray),
          ("aggCols" -> x.params_agg.aggCols.toArray)
        )
       )
      case "Join" => new ConfigOpsMap("Join",
        Map(
          ("join_type" -> x.params_join.join_type),
          ("join_to_csv_dataset" -> x.params_join.join_to_csv_dataset),
          ("join_expr" -> x.params_join.join_expr)
        )
      )
    }

    if(thisOps != None)
      ops += thisOps
  }

  // For backup only - deprecated
  var ops_BAK = new ArrayBuffer[ConfigOps]()
  ops_BAK += new ConfigOpsArr("Filter", Array("headway >= 0"))
  ops_BAK += new ConfigOpsStrTuples("Rename", Array(("direction_name","compass_bearing")))
  ops_BAK += new ConfigOpsArr("Drop",
    Array(
      "site_id", /*"date",*/ "lane", "lane_name", "direction",
      /*"direction_name",*/ "reverse", "class_scheme", "class", /*"class_name",*/ "length", "headway", "gap",
      /*"speed",*/ "weight","vehicle_id", "flags", "flag_text", "num_axles", "axle_weights", "axle_spacings"
    )
  )
  ops_BAK += new ConfigOpsStrTuples("Add",
      Array(
        ("class_name_up", "upper(class_name)"),
        ("date", "to_timestamp(date)")
      )
    )

  ops_BAK += new ConfigOpsAgg("Agg",
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
