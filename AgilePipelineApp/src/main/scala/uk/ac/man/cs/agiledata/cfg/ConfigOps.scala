package uk.ac.man.cs.agiledata.cfg

import org.apache.spark.sql.Column

/**
 * This series of classes are used to instantiate the ops configuration to be used in WFConfigOps
 *
 * The base class is the ConfigOps
 *
 * @param order order of the operation
 * @param name name identifier of the operation type
 */
class ConfigOps(order: String, name: String) {
  def getOpsOrder(): Int = {
    def toInt(s: String): Int = {
      try {
        s.toInt
      } catch {
        case e: Exception => 0
      }
    }

    return toInt(order)
  }
  def getOpsName(): String = {
    return name
  }
}

/**
 * Child class that inherits the ConfigOps attributes
 * Contains parameter of Array of String
 *
 * @param order order of the operation
 * @param name name identifier of the operation type
 * @param paramArr array of string of the ops parameter
 */
class ConfigOpsArr(order: String, name: String, paramArr: Array[String]) extends ConfigOps(order, name) {
  def getOpsArrParams(): Array[String] = {
    return paramArr
  }
}

/**
 * Child class that inherits the ConfigOps attributes
 * Contains parameter of Array of (String,Column)
 *
 * @param order order of the operation
 * @param name name identifier of the operation type
 * @param paramTupleArr array of tuple that contains String and Column
 */
class ConfigOpsColTuples(order: String, name: String, paramTupleArr: Array[(String,Column)]) extends ConfigOps(order, name) {
  def getOpsTuplesParams(): Array[(String,Column)] = {
    return paramTupleArr
  }
}

/**
 * Child class that inherits the ConfigOps attributes
 * Contains parameter of Array of (String,String)
 *
 * @param order order of the operation
 * @param name name identifier of the operation type
 * @param paramTupleArr array of tuple that contains String and String
 */
class ConfigOpsStrTuples(order: String, name: String, paramTupleArr: Array[(String,String)]) extends ConfigOps(order, name) {
  def getOpsTuplesParams(): Array[(String,String)] = {
    return paramTupleArr
  }
}

/**
 * Child class that inherits the ConfigOps attributes
 * Contains parameter of Map of (String,String)
 *
 * @param order order of the operation
 * @param name name identifier of the operation type
 * @param paramMap map of String to another String
 */
class ConfigOpsMap(order: String, name: String, paramMap: Map[String,String]) extends ConfigOps(order, name) {
  def getOpsMapParams(): Map[String,String] = {
    return paramMap
  }
}

/**
 * Child class that inherits the ConfigOps attributes
 * Contains parameter of Map of (String,String)
 * And Map of [String,Array[String]]
 *
 * @param order order of the operation
 * @param name name identifier of the operation type
 * @param paramAddMap map of String to another String
 * @param multiParam map of String to Array of String
 */
class ConfigOpsAgg(order: String, name: String, paramAddMap: Map[String,String], multiParam: Map[String,Array[String]]) extends ConfigOpsMap(order, name, paramAddMap) {
  def getMultiParam(): Map[String,Array[String]] = {
    return multiParam
  }
}

