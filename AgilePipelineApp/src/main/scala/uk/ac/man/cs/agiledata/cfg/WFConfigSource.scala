package uk.ac.man.cs.agiledata.cfg

class ConfigSourceTargetMap(paramSource: Map[String,String]) {
  def getMap(): Map[String,String] = {
    return paramSource
  }
}

class WFConfigSource(configFromDB : WFConfig) {

  val sourceWF = new ConfigSourceTargetMap(
    Map(
      "type" -> configFromDB.workflow.source.source_type,
      "broker" -> configFromDB.workflow.source.broker,
      "topic" -> configFromDB.workflow.source.topic,
      "startingOffsets" -> configFromDB.workflow.source.startingOffsets
    )
  )

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

class WFConfigTarget(configFromDB : WFConfig) {

  val sourceWF = new ConfigSourceTargetMap(
    Map(
      "type" -> configFromDB.workflow.target.target_type,
      "broker" -> configFromDB.workflow.target.broker,
      "topic" -> configFromDB.workflow.target.topic,
      "checkpointLocation" -> configFromDB.workflow.target.checkpointLocation
    )
  )

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
