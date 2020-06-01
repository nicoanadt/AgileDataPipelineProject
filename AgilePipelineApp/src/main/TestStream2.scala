object TestStream2 {
  val broker = "kafka:9090"

  //Can have multiple topics separated by comma
  val topic_source = "trafficTopic1"
  val topic_target = "trafficTopic2"


  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", broker)
    .option("subscribe", topic_source)
    .load()

  val ds = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .as[(String, String)]






  ds
    .writeStream // use `write` for batch, like DataFrame
    .format("kafka")
    .option("kafka.bootstrap.servers", broker)
    .option("topic", topic_target)
    .option("checkpointLocation", "/tmp/apps/checkpoint")
    .start()


}
