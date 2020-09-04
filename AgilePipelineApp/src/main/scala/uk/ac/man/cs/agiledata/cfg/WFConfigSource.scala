package uk.ac.man.cs.agiledata.cfg

/**
 * Simply map a string to another string
 * @param paramSource
 */
class ConfigSourceTargetMap(paramSource: Map[String,String]) {
  def getMap(): Map[String,String] = {
    return paramSource
  }
}

/**
 * This class is responsible to convert workflow configuration from the database to the
 * WFConfigSource object that can be read by the AdalineRunApp
 *
 * @param configFromDB  WFConfig object obtained from the MongoDB metastore database
 */
class WFConfigSource(configFromDB : WFConfig) {

  val sourceWF = new ConfigSourceTargetMap(
    Map(
      "type" -> configFromDB.workflow.source.source_type,
      "broker" -> configFromDB.workflow.source.broker,
      "topic" -> configFromDB.workflow.source.topic,
      "startingOffsets" -> configFromDB.workflow.source.startingOffsets
    )
  )

  //This config is not used
  val sourceWF_BAK = new ConfigSourceTargetMap(
    Map(
      "type" -> "kafka",
      "broker" -> "kafka:9090",
      "topic" -> "trafficTopicNow",
      "startingOffsets" -> "earliest"
    )
  )

  def getConfigSource():  ConfigSourceTargetMap ={
    return sourceWF
  }
}

/**
 * This class is responsible to convert workflow configuration from the database to the
 * WFConfigTarget object that can be read by the AdalineRunApp
 *
 * @param configFromDB  WFConfig object obtained from the MongoDB metastore database
 */
class WFConfigTarget(configFromDB : WFConfig) {

  val sourceWF = new ConfigSourceTargetMap(
    Map(
      "type" -> configFromDB.workflow.target.target_type,
      "broker" -> configFromDB.workflow.target.broker,
      "topic" -> configFromDB.workflow.target.topic,
      "checkpointLocation" -> configFromDB.workflow.target.checkpointLocation
    )
  )

  //This config is not used
  val sourceWF_BAK = new ConfigSourceTargetMap(
    Map(
      "type" -> "kafka",
      "broker" -> "kafka:9090",
      "topic" -> "trafficTopicAvg",
      "checkpointLocation" -> "/tmp/apps/checkpoint"
    )
  )

  def getConfigTarget():  ConfigSourceTargetMap ={
    return sourceWF
  }
}
