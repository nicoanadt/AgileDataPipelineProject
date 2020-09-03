package uk.ac.man.cs.agiledata.cfg

import org.apache.spark.sql.Column

/**
 * Base class
 * @param name
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
 * Child class
 * @param name
 * @param paramArr
 */
class ConfigOpsArr(order: String, name: String, paramArr: Array[String]) extends ConfigOps(order, name) {
  def getOpsArrParams(): Array[String] = {
    return paramArr
  }
}

/**
 * Child class
 * @param name
 * @param paramTupleArr
 */
class ConfigOpsColTuples(order: String, name: String, paramTupleArr: Array[(String,Column)]) extends ConfigOps(order, name) {
  def getOpsTuplesParams(): Array[(String,Column)] = {
    return paramTupleArr
  }
}

/**
 * Child class
 * @param name
 * @param paramTupleArr
 */
class ConfigOpsStrTuples(order: String, name: String, paramTupleArr: Array[(String,String)]) extends ConfigOps(order, name) {
  def getOpsTuplesParams(): Array[(String,String)] = {
    return paramTupleArr
  }
}

class ConfigOpsMap(order: String, name: String, paramMap: Map[String,String]) extends ConfigOps(order, name) {
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
class ConfigOpsAgg(order: String, name: String, paramAddMap: Map[String,String], multiParam: Map[String,Array[String]]) extends ConfigOpsMap(order, name, paramAddMap) {
  def getMultiParam(): Map[String,Array[String]] = {
    return multiParam
  }
}

