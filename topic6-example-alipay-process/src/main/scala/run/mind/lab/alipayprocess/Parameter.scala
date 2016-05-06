package run.mind.lab.alipayprocess

case class Parameter(
                      spark_master: String = "local[2]",
                      aplipay_address: String = "hdfs://bd:9000/user/alipay",
                      hive_db: String = "alipay"
                    ) extends Serializable