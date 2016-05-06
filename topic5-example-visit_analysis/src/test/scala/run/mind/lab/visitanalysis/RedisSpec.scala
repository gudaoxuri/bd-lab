package run.mind.lab.visitanalysis

import org.scalatest.FunSuite
import run.mind.lab.visitanalysis.helper.RedisHelper

class RedisSpec extends FunSuite {

  test("Redis test") {
    RedisHelper.init("192.168.99.100:6379",0)
    val inst = RedisHelper.get()
    inst.hset("test", "k1", "v1")
    inst.hset("test", "k2", "v2")
    val values = inst.hgetAll("test")
    assert(values.size() == 2)
    assert(values.containsKey("k1") && values.get("k1") == "v1")
  }


}


