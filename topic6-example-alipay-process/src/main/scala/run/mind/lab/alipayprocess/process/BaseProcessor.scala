package run.mind.lab.alipayprocess.process

import java.sql.Date
import java.text.SimpleDateFormat

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{Logging, SparkContext}
import run.mind.lab.alipayprocess.Parameter

case class FinanceData(category: String, orderNum: String, alipayId: String, price: BigDecimal, item: String, orderYear: Int, orderMonth: Int, orderDate: Int, orderTime: java.sql.Date) extends Serializable

case class BaseProcessor(@transient sc: SparkContext, @transient hc: HiveContext, parameter: Parameter) extends Serializable with Logging {

  val sqlContext = new HiveContext(sc)

  import sqlContext.implicits._

  private val mdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private val df = new SimpleDateFormat("yyyyMMdd")
  private val mf = new SimpleDateFormat("yyyyMM")
  private val yf = new SimpleDateFormat("yyyy")

  def process(): Unit = {
    log.info("=====>> Start process")
   /* hc.sql(
      s"""
         |CREATE TABLE IF NOT EXISTS ${parameter.hive_db}.finance (
         |   category STRING,
         |   orderNum STRING,
         |   alipayId STRING,
         |   price FLOAT,
         |   item STRING,
         |   orderYear INT,
         |   orderMonth INT,
         |   orderDate INT,
         |   orderTime TIMESTAMP
         | ) STORED AS orc""".stripMargin)*/
    val financeData = sc.textFile(parameter.aplipay_address).map(_.split("\t")).filter(_.length == 11).map {
      item =>
        val orderTime = mdf.parse(item(10))
        FinanceData(item(1), item(2), item(4), BigDecimal(item(6)), item(9),
          yf.format(orderTime).toInt, mf.format(orderTime).toInt, df.format(orderTime).toInt, new Date(orderTime.getTime))
    }
    financeData.cache()
    financeData.toDF().write.format("orc").mode(SaveMode.Overwrite).saveAsTable(parameter.hive_db + ".finance")
    tradeAmountByMonthlyProcess(financeData)
    log.info("=====>> Successful process")
  }

  case class TradeAmountByMonthlyData(alipayId: String, orderMonth: Int, price: BigDecimal) extends Serializable

  /**
    * 月交易额
    */
  def tradeAmountByMonthlyProcess(financeData: RDD[FinanceData]): Unit = {
    financeData.filter(_.category == "交易付款").groupBy(i => i.alipayId + "," + i.orderMonth).map {
      item =>
        val shopId = item._1.split(",")(0)
        val orderMonth = item._1.split(",")(1).toInt
        val tradeAmount = item._2.map(_.price).sum
        TradeAmountByMonthlyData(shopId, orderMonth, tradeAmount)
    }.toDF.write.format("orc").mode(SaveMode.Overwrite).saveAsTable(parameter.hive_db + ".trade_amount_monthly")
  }

}



