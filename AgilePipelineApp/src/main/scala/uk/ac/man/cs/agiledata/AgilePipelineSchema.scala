package uk.ac.man.cs.agiledata

import org.apache.spark.sql.types.{FloatType, IntegerType, StringType, StructType}
import uk.ac.man.cs.agiledata.cfg.ConfigSchemaTuples

/**
 * This class converts workflow configuration from database to a dataset structure
 * The dataset structure consists of column name and data type
 */
class AgilePipelineSchema {

  /**
   * Converts schema from database to usable schema for a dataframe
   *
   * @param param Array of string pair: column name and data type
   * @return
   */
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
