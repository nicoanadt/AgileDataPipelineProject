package uk.ac.man.cs.agiledata.cfg

class ConfigSourceTargetMap(paramSource: Map[String,String]) {
  def getMap(): Map[String,String] = {
    return paramSource
  }
}

class WFConfigSource {
  val sourceWF = new ConfigSourceTargetMap(
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

class WFConfigTarget {
  val sourceWF = new ConfigSourceTargetMap(
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
