package run.mind.lab.visitanalysis

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{Logging, SparkConf}
import run.mind.lab.visitanalysis.process.BaseProcessor

object Startup extends Logging {

  def main(args: Array[String]): Unit = {
    // 参数收集
    val parser = new scopt.OptionParser[Parameter]("Visit_Analysis") {
      head("Visit Analysis", "1.0")
      opt[String]('s', "spark_master") action { (x, c) =>
        c.copy(spark_master = x)
      } text "spark master address , e.g. local[2]"
      opt[Int]('t', "batch_duration") action { (x, c) =>
        c.copy(spark_streamingBatchDuration = x)
      } text "spark streaming batch duration (s) , default 10"
      opt[String]("checkpoint_address") action { (x, c) =>
        c.copy(checkpoint_address = x)
      } text " checkpoint address , e.g. hdfs://127.0.0.1:9000/tmp/checkpoint defalut /tmp/checkpoint"
      opt[String]('z', "zk_hosts") required() action { (x, c) =>
        c.copy(zookeeper_hosts = x)
      } text "zookeeper hosts , e.g. 127.0.0.1:2181"
      opt[String]("kafka_group") action { (x, c) =>
        c.copy(kafka_group = x)
      } text "kafka group name ,default default"
      opt[String]("kafka_topic") required() action { (x, c) =>
        c.copy(kafka_topic = x)
      } text "kafka topic name , e.g. topic1"
      opt[String]("mongo_address") required() action { (x, c) =>
        c.copy(mongo_address = x)
      } text "mongo address , e.g. localhost:27017"
      opt[String]("mongo_db") required() action { (x, c) =>
        c.copy(mongo_db = x)
      } text "mongo db , myDb"
      opt[String]("redis_address") required() action { (x, c) =>
        c.copy(redis_address = x)
      } text "redis address , e.g. 127.0.0.1:6379"
      opt[Int]("redis_db") action { (x, c) =>
        c.copy(redis_db = x)
      } text "redis db , default 0"
      opt[String]("ip_path") required() action { (x, c) =>
        c.copy(ip_path = x)
      } text "ip path , e.g. /opt/ip.bat"
    }
    parser.parse(args, Parameter()) match {
      case Some(parameter) =>
        start(parameter)
      case None =>
        log.error("Visit Analysis start error.")
        System.exit(1)
    }
  }

  /**
    * 输入参数正确，开始启动Spark
    */
  def start(parameter: Parameter) {
    log.info("Starting Visit Analysis")
    // 定义 Spark配置
    val sparkConf = new SparkConf().setAppName("Visit Analysis")
    // 指定Master
    if (parameter.spark_master != null) {
      sparkConf.setMaster(parameter.spark_master)
    }
      // 解决1.6.1的一个bug:
      //  WARN NettyRpcEndpointRef: Error sending message [message = Heartbeat(0,[Lscala.Tuple2;@50ef2b31,BlockManagerId(0, bd, 53790))] in 1 attempts
      //  org.apache.spark.rpc.RpcTimeoutException: Futures timed out after [10 seconds]. This timeout is controlled by spark.executor.heartbeatInterval
      // https://issues.apache.org/jira/browse/SPARK-13906
      .set("spark.rpc.netty.dispatcher.numThreads", "2")
    // 定义Streaming上下文
    val ssc = new StreamingContext(sparkConf, Seconds(parameter.spark_streamingBatchDuration))
    // 设置检查点
    ssc.checkpoint(s"${parameter.checkpoint_address}")

    log.info("Connection Kafka Server.")
    val lines = KafkaUtils.createStream(
      ssc,
      parameter.zookeeper_hosts,
      parameter.kafka_group,
      Map(parameter.kafka_topic -> 2),
      StorageLevel.MEMORY_AND_DISK_SER
    ).map(_._2)
    //if (log.isDebugEnabled || log.isTraceEnabled) {
    lines.print(3)
    //}
    // Process...
    // 将Dstream转成RDD
    lines.foreachRDD {
      rdd =>
        BaseProcessor.process(rdd, parameter)
    }
    ssc.start()
    log.info("Started Visit Analysis")
    ssc.awaitTermination()

  }

}



