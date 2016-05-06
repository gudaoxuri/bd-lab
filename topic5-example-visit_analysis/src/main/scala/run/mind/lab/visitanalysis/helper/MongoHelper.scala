package run.mind.lab.visitanalysis.helper

import com.mongodb.client.{MongoCollection, MongoDatabase}
import com.mongodb.{MongoClient, MongoClientURI}
import org.apache.spark.Logging
import org.bson.Document

object MongoHelper extends Serializable with Logging {

  private var db: MongoDatabase = _

  /**
    * 初始化
    *
    * @param mongoAddress host:port
    * @param dbName       db
    */
  def init(mongoAddress: String, dbName: String): Unit = {
    db = new MongoClient(new MongoClientURI("mongodb://" + mongoAddress)).getDatabase(dbName)
  }

  def get(collection: String): MongoCollection[Document] = {
    db.getCollection(collection)
  }

}
