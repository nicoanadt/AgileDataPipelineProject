
import org.apache.spark.streaming._
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

val kafkaParams = Map[String, Object](
  "bootstrap.servers" -> "kafka:9090",
  "key.deserializer" -> classOf[StringDeserializer],
  "value.deserializer" -> classOf[StringDeserializer],
  "group.id" -> "g1",
  "auto.offset.reset" -> "earliest",
  "enable.auto.commit" -> (false: java.lang.Boolean)
)

//Can have multiple topics separated by comma
val topics = Array("sample")

// Accumulating results in batches of
val batchInterval = Seconds(10)

// How many batches to run before terminating
val batchesToRun = 3

val streamingContext = new StreamingContext(sc,batchInterval)

val dstream = KafkaUtils.createDirectStream[String, String](
  streamingContext,
  PreferConsistent,
  Subscribe[String, String](topics, kafkaParams)
)

val messages = dstream.map(record => (record.key, record.value))

//messages.print()

messages.foreachRDD { rdd =>

  println("Processed messages in this batch: " + rdd.count())
  //val df = rdd.toDF()
  //df.printSchema()
  //df.select("_2").show(false)
  
  // Transform to DF based on json schema 
  // TODO: use predefined schema(?)
  val rddJson = rdd.map(_._2.toString)
  val df = spark.read.json(rddJson)
  
  df.printSchema()
  df.show(false)
  

  
}


streamingContext.start()
