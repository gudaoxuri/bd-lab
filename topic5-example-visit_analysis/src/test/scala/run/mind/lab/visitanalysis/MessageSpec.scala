package run.mind.lab.visitanalysis

import java.util.Date

import com.mongodb.client.model.Filters
import org.scalatest.FunSuite
import run.mind.lab.visitanalysis.helper.MongoHelper
import run.mind.lab.visitanalysis.process.BaseProcessor

class MessageSpec extends FunSuite {

  test("Message test") {
    IP.load(this.getClass.getResource("/").getPath + "ip.dat")
    MongoHelper.init("192.168.99.100:27017", "visit")
    BaseProcessor.doProcess(VisitDTO("http://www.baidu.com|122.255.21.25|wfjosms=sfwtksdksetsd"))
    BaseProcessor.doProcess(VisitDTO("http://www.baidu.com|122.255.21.25|wfjosms=sfwtksdksetsd"))
    BaseProcessor.doProcess(VisitDTO("http://news.13.com|272.255.21.25|wfjosms=sfwtksdksetsd"))
    var findResult = MongoHelper.get("statistics").find(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", "www.baidu.com"), Filters.eq("date", 0)))
    assert(findResult.first().getLong("count") == 2L)
    findResult = MongoHelper.get("statistics").find(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", "www.baidu.com"), Filters.eq("date", BaseProcessor.df.format(new Date()).toLong)))
    assert(findResult.first().getLong("count") == 2L)
    findResult = MongoHelper.get("statistics").find(Filters.and(Filters.eq("category", "host"), Filters.eq("clue", "news.13.com"), Filters.eq("date", BaseProcessor.df.format(new Date()).toLong)))
    assert(findResult.first().getLong("count") == 1L)
  }


}


