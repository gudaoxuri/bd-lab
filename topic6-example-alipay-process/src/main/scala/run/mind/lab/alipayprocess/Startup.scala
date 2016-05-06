package run.mind.lab.alipayprocess

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{Logging, SparkConf, SparkContext}
import run.mind.lab.alipayprocess.process.BaseProcessor

object Startup extends Logging {

  def main(args: Array[String]): Unit = {
    // 参数收集
    val parser = new scopt.OptionParser[Parameter]("Alipay Process") {
      head("Alipay Process", "1.0")
      opt[String]('s', "spark_master") required() action { (x, c) =>
        c.copy(spark_master = x)
      } text "spark master address , e.g. local[2]"
      opt[String]('a', "aplipay_address") action { (x, c) =>
        c.copy(aplipay_address = x)
      } text "aplipay address , default hdfs://bd:9000/user/alipay"
      opt[String]('h', "hive_db") action { (x, c) =>
        c.copy(hive_db = x)
      } text " hive db , default alipay"
    }
    parser.parse(args, Parameter()) match {
      case Some(parameter) =>
        start(parameter)
      case None =>
        log.error("Alipay Process start error.")
        System.exit(1)
    }
  }

  /**
    * 输入参数正确，开始启动Spark
    */
  def start(parameter: Parameter) {
    log.info("Starting Alipay Process")
    // 定义 Spark配置
    val sparkConf = new SparkConf()
      // 指定Master
      .setMaster(parameter.spark_master)
      .setAppName("Alipay Process")
    val sc = new SparkContext(sparkConf)
    val hc = new HiveContext(sc)
    new BaseProcessor(sc, hc, parameter).process()
    log.info("Started Alipay Process")

  }

}



