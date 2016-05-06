package run.mind.lab.visitanalysis

case class Parameter(
                      spark_master: String = null,
                      spark_streamingBatchDuration: Int = 10,
                      checkpoint_address: String = "/tmp/checkpoint",
                      zookeeper_hosts: String = "127.0.0.1:2181",
                      kafka_group: String = "default",
                      kafka_topic: String = "",
                      mongo_address: String = "127.0.0.1:27017",
                      mongo_db: String = "",
                      redis_address: String = "127.0.0.1:6379",
                      redis_db: Int = 0,
                      ip_path: String = ""
                    ) extends Serializable