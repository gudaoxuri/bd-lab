package run.mind.lab.visitanalysis.process

import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

import com.mongodb.client.model.{Filters, Updates}
import org.apache.spark.Logging
import org.apache.spark.rdd.RDD
import org.bson.Document
import run.mind.lab.visitanalysis.helper.{MongoHelper, RedisHelper}
import run.mind.lab.visitanalysis.{IP, Parameter, VisitDTO}


object BaseProcessor extends Serializable with Logging {

  def process(rdd: RDD[String], parameter: Parameter): Unit = {
    rdd.foreachPartition {
      pRecords =>
        // 初始化Redis连接
        RedisHelper.init(parameter.redis_address, parameter.redis_db)
        // 初始化Mongo连接
        MongoHelper.init(parameter.mongo_address, parameter.mongo_db)
        // 加载IP地址库
        IP.load(parameter.ip_path)
        // val jedis = RedisHelper.get()
        pRecords.foreach {
          message =>
            //  jedis.incr("all:"+new URL(message.url).getHost)
            log.debug(s"Received a message : $message")
            val msg = VisitDTO(message)
            if (msg != null) {
              doProcess(msg)
            } else {
              log.warn(s"Received an error message : $message")
            }
        }
      //  jedis.close()
    }
  }

  val df = new SimpleDateFormat("yyyyMMdd")

  def doProcess(message: VisitDTO): Unit = {
    val url = new URL(message.url)
    val host = url.getHost
    val date = df.format(new Date()).toLong

    var ip: Array[String] = null
    try {
      ip = IP.find(message.ip)
    } catch {
      case _: Throwable =>
        ip = IP.find("127.0.0.1")
    }
    val ip_addr = ip.mkString(" ")
    val ip_country = ip(0)
    val ip_province = ip(1)
    val ip_city = ip(2)
    val ip_county = ip(3)

    val statisticsCollection = MongoHelper.get("statistics")
    if (statisticsCollection.count(Filters.and(Filters.eq("category", ""), Filters.eq("clue", ""), Filters.eq("date", 0))) == 0) {
      statisticsCollection.insertOne(new Document("category", "").append("clue", "").append("date", 0).append("count", 0L))
    }
    statisticsCollection.updateOne(Filters.and(Filters.eq("category", ""), Filters.eq("clue", ""), Filters.eq("date", 0)), Updates.inc("count", 1))
    if (statisticsCollection.count(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", host), Filters.eq("date", 0))) == 0) {
      statisticsCollection.insertOne(new Document("category", "host").append("clue", host).append("date", 0).append("count", 0L))
    }
    statisticsCollection.updateOne(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", host), Filters.eq("date", 0)), Updates.inc("count", 1))
    if (statisticsCollection.count(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", host), Filters.eq("date", date))) == 0) {
      statisticsCollection.insertOne(new Document("category", "host").append("clue", host).append("date", date).append("count", 0L))
    }
    statisticsCollection.updateOne(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", host), Filters.eq("date", date)), Updates.inc("count", 1))
    if (statisticsCollection.count(Filters.and(Filters.eq("category", "province"), Filters.eq("clue", ip_province), Filters.eq("date", 0))) == 0) {
      statisticsCollection.insertOne(new Document("category", "province").append("clue", ip_province).append("date", 0).append("count", 0L))
    }
    statisticsCollection.updateOne(Filters.and(Filters.eq("category", "province"), Filters.eq("clue", ip_province), Filters.eq("date", 0)), Updates.inc("count", 1))
    if (statisticsCollection.count(Filters.and(Filters.eq("category", "province"), Filters.eq("clue", ip_province), Filters.eq("date", date))) == 0) {
      statisticsCollection.insertOne(new Document("category", "province").append("clue", ip_province).append("date", date).append("count", 0L))
    }
    statisticsCollection.updateOne(Filters.and(Filters.eq("category", "province"), Filters.eq("clue", ip_province), Filters.eq("date", date)), Updates.inc("count", 1))
    MongoHelper.get("logs").insertOne(new Document("url", message.url).append("ip", message.ip).append("cookies", message.cookies).append("time", System.currentTimeMillis()))
  }
}



