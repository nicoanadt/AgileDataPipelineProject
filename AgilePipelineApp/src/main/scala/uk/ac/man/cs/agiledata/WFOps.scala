package uk.ac.man.cs.agiledata

import org.apache.spark.sql.Column

/**
 * Base class
 * @param name
 */
class WFOps(name: String) {
  def getOpsName(): String = {
    return name
  }
}

/**
 * Child class
 * @param name
 * @param paramArr
 */
class WFOpsArr(name: String, paramArr: Array[String]) extends WFOps(name) {
  def getOpsParams(): Array[String] = {
    return paramArr
  }
}

/**
 * Child class
 * @param name
 * @param paramAddArr
 */
class WFOpsTuples(name: String, paramTupleArr: Array[(String,Column)]) extends WFOps(name) {
  def getOpsTuplesParams(): Array[(String,Column)] = {
    return paramTupleArr
  }
}

//class WFOpsAdd(name: String, paramAddMap: Map[String,Column]) extends WFOps(name) {
//  def getOpsAddParams(): Map[String,Column] = {
//    return paramAddMap
//  }
//}

