package uk.ac.man.cs.agiledata

import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}

class AgilePipelineSchema {
  def getStruct(param: ConfigSchemaTuples): StructType = {
    var returnStruct: StructType = new StructType()

    for (tuple <- param.getConfigSchema()) {
      returnStruct = tuple._2 match {
        case "string" => returnStruct.add(tuple._1,StringType, true)
        case "int" => returnStruct.add(tuple._1,IntegerType, true)
        case "float" => returnStruct.add(tuple._1,FloatType, true)
      }
    }

    return returnStruct
  }
}
