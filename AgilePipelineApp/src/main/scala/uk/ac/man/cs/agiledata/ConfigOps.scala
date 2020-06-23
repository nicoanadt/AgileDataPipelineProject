package uk.ac.man.cs.agiledata

import org.apache.spark.sql.Column

/**
 * Base class
 * @param name
 */
class ConfigOps(name: String) {
  def getOpsName(): String = {
    return name
  }
}

/**
 * Child class
 * @param name
 * @param paramArr
 */
class ConfigOpsArr(name: String, paramArr: Array[String]) extends ConfigOps(name) {
  def getOpsArrParams(): Array[String] = {
    return paramArr
  }
}

/**
 * Child class
 * @param name
 * @param paramTupleArr
 */
class ConfigOpsColTuples(name: String, paramTupleArr: Array[(String,Column)]) extends ConfigOps(name) {
  def getOpsTuplesParams(): Array[(String,Column)] = {
    return paramTupleArr
  }
}

/**
 * Child class
 * @param name
 * @param paramTupleArr
 */
class ConfigOpsStrTuples(name: String, paramTupleArr: Array[(String,String)]) extends ConfigOps(name) {
  def getOpsTuplesParams(): Array[(String,String)] = {
    return paramTupleArr
  }
}

class ConfigOpsMap(name: String, paramMap: Map[String,String]) extends ConfigOps(name) {
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
class ConfigOpsAgg(name: String, paramAddMap: Map[String,String], multiParam: Map[String,Array[String]]) extends ConfigOpsMap(name, paramAddMap) {
  def getMultiParam(): Map[String,Array[String]] = {
    return multiParam
  }
}

