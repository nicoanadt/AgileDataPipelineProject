package uk.ac.man.cs.agiledata

import org.apache.spark.sql.Column

/**
 * Base class
 * @param name
 */
class WFConfigOps(name: String) {
  def getOpsName(): String = {
    return name
  }
}

/**
 * Child class
 * @param name
 * @param paramArr
 */
class WFConfigOpsArr(name: String, paramArr: Array[String]) extends WFConfigOps(name) {
  def getOpsArrParams(): Array[String] = {
    return paramArr
  }
}

/**
 * Child class
 * @param name
 * @param paramTupleArr
 */
class WFConfigOpsColTuples(name: String, paramTupleArr: Array[(String,Column)]) extends WFConfigOps(name) {
  def getOpsTuplesParams(): Array[(String,Column)] = {
    return paramTupleArr
  }
}

/**
 * Child class
 * @param name
 * @param paramTupleArr
 */
class WFConfigOpsStrTuples(name: String, paramTupleArr: Array[(String,String)]) extends WFConfigOps(name) {
  def getOpsTuplesParams(): Array[(String,String)] = {
    return paramTupleArr
  }
}

class WFConfigOpsMap(name: String, paramMap: Map[String,String]) extends WFConfigOps(name) {
  def getOpsMapParams(): Map[String,String] = {
    return paramMap
  }
}

/**
 * strParam Map(String,String)
 * multiParam Map(String,Array[String])
 *
 * @param name
 * @param paramAddMap
 * @param aggParam
 */
class WFConfigOpsAgg(name: String, paramAddMap: Map[String,String], multiParam: Map[String,Array[String]]) extends WFConfigOpsMap(name, paramAddMap) {
  def getMultiParam(): Map[String,Array[String]] = {
    return multiParam
  }
}

