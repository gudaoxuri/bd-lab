package run.mind.lab.visitanalysis.helper

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.spark.Logging
import redis.clients.jedis.{Jedis, JedisPool}

object RedisHelper extends Serializable with Logging {

  @transient private var instance: JedisPool = _
  @transient private var dbIdx: Int = _

  /**
    * 初始化
    *
    * @param address host:port
    * @param _dbIdx  dbIdx
    */
  def init(address: String, _dbIdx: Int): Unit = {
    if (instance == null) {
      log.info(s"Initialization Redis pool at $address")
      instance = new JedisPool(new GenericObjectPoolConfig(), address.split(":")(0), address.split(":")(1).toInt, 30000)
      dbIdx = _dbIdx
    }
  }

  def get(db: Int = dbIdx): Jedis = {
    val jedis = instance.getResource
    jedis.select(db)
    jedis
  }

}
