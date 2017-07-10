package memory

/**
  * Created by vnaveen0 on 9/7/17.
  */

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import chisel3._
import config._
import arbiters._
import memory._


class ReadWriteArbiterTests01(c: => ReadWriteArbiter) (implicit p: config.Parameters)
  extends PeekPokeTester(c) {
  // -- IO ---
  //    val ReadCacheReq = Decoupled(new CacheReq)
  //    val ReadCacheResp = Flipped(Valid(new CacheResp))
  //    val WriteCacheReq = Decoupled(new CacheReq)
  //    val WriteCacheResp = Flipped(Valid(new CacheResp))
  //    val CacheReq = Decoupled(new CacheReq)
  //    val CacheResp = Flipped(Valid(new CacheRespT))



  var time =  -10
  for (t <- 0 until 10) {
    println(s"t = ${t} ------------------------- ")

    if(t > 1 ) {
      poke(c.io.CacheReq.ready,1)
    }

    if(t > 3 && t < 8) {
      if (peek(c.io.WriteCacheReq.ready) == 1) {
        println(s" WriteCacheReq Ready ")
        poke(c.io.WriteCacheReq.valid, 1)
        poke(c.io.WriteCacheReq.bits.addr, 23)
        poke(c.io.WriteCacheReq.bits.data, 4)
        poke(c.io.WriteCacheReq.bits.iswrite, 1)
        poke(c.io.WriteCacheReq.bits.tag, 1)
      }
      else {
        poke(c.io.WriteCacheReq.valid, 0)
      }
    }
    else {
      poke(c.io.WriteCacheReq.valid, 0)
    }


    if(t== 4) {
      if (peek(c.io.ReadCacheReq.ready) == 1) {
        println(s" ReadCacheReq Ready ")
        poke(c.io.ReadCacheReq.valid, 1)
        poke(c.io.ReadCacheReq.bits.addr, 54)
        poke(c.io.ReadCacheReq.bits.data, 434342432)
        poke(c.io.ReadCacheReq.bits.iswrite, 0)
        poke(c.io.ReadCacheReq.bits.tag, 1)
      }
      else {
        poke(c.io.ReadCacheReq.valid, 0)
      }
    }
    else {
      poke(c.io.ReadCacheReq.valid, 0)
    }

    if(peek(c.io.CacheReq.valid) == 1) {

      println(s" IO CacheReq isWrite  ${peek(c.io.CacheReq.bits.iswrite)}")
      println(s" IO CacheReq data     ${peek(c.io.CacheReq.bits.data)}")
      println(s" IO CacheReq tag      ${peek(c.io.CacheReq.bits.tag)}")


      time = t+1

      println(s" Sending Response from Cache ")
      poke(c.io.CacheResp.bits.data, 45)
      poke(c.io.CacheResp.bits.isSt, peek(c.io.CacheReq.bits.iswrite))
      poke(c.io.CacheResp.bits.tag, peek(c.io.CacheReq.bits.tag))
      poke(c.io.CacheResp.valid, 1)
    }
    else {
      poke(c.io.CacheResp.valid, 0)
    }


    println(s" IO CacheReq Valid  ${peek(c.io.CacheReq.valid)}")
    if(peek(c.io.ReadCacheResp.valid) == 1) {

      println(s"^^^^^^^^^^^^^^")
      println(s"ReadCacheResp :  -------------")
      println(s" IO ReadResp Valid  ${peek(c.io.ReadCacheResp)}")
    }


    if(peek(c.io.WriteCacheResp.valid) == 1) {

      println(s"^^^^^^^^^^^^^^")
      println(s"WriteCacheResp :  -------------")
      println(s" IO WriteResp Valid  ${peek(c.io.WriteCacheResp)}")
    }



    step(1)
  }


}


class ReadWriteArbiterTester01 extends  FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  it should "Memory Controller tester" in {
    chisel3.iotesters.Driver(() => new ReadWriteArbiter()(p)) {
      c => new ReadWriteArbiterTests01(c)
    } should be(true)
  }
}


//    if(peek(c.io.ReadCacheResp.valid) == 1) {
//      println(" ReadCacheResp  Received ")
//    }
//
//
//    if(peek(c.io.WriteCacheResp.valid) == 1) {
//      println(" WriteCacheResp  Received ")
//    }

