package memory

// /**
//   * Created by vnaveen0 on 2/6/17.
//   */

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import config._

class MemoryControllerTests(c: MemoryController)(implicit p: config.Parameters) extends PeekPokeTester(c) {

	var readidx = 0
	poke(c.io.ReadIn(0).bits.address, 10)
	poke(c.io.ReadIn(0).bits.RouteID, 0)
	poke(c.io.ReadIn(0).bits.Typ,2)
	poke(c.io.ReadIn(0).valid,1)
	poke(c.io.ReadIn(1).bits.address, 10)
	poke(c.io.ReadIn(1).bits.RouteID, 1)
	poke(c.io.ReadIn(1).bits.Typ,2)
	poke(c.io.ReadIn(1).valid,1)
	var req  = false
	var tag  = peek(c.io.CacheReq.bits.tag)	
	var reqT = 0
    // in_arb.io.in(0).bits.RouteID := 0.U
    // in_arb.io.in(0).bits.Typ := MT_W
    // in_arb.io.in(0).valid := true.B
	poke(c.io.CacheReq.ready,1)
	poke(c.io.CacheResp.valid,false)
	for (t <- 0 until 12) {
		if((peek(c.io.CacheReq.valid) == 1) && (peek(c.io.CacheReq.ready) == 1)) {
			printf(s"t: ${t} ---------------------------- \n")
			req  = true
			tag  = peek(c.io.CacheReq.bits.tag)
			reqT = t
		}
		if ((req == true) && (t > reqT))
		{
			poke(c.io.CacheResp.valid,true)
			poke(c.io.CacheResp.bits.data, 0xdeadbeefL)
			printf("Tag:%d",tag)
			poke(c.io.CacheResp.bits.tag,peek(c.io.CacheReq.bits.tag))
			req = false
		}
		if (req == true) {
			poke(c.io.CacheReq.ready,false)
		} else {
			poke(c.io.CacheReq.ready,true)
		}

//     if(t<11) {
//       poke(c.io.ReadIn(1).valid, false)
//       poke(c.io.ReadIn(2).valid, false)

//       //-------------------------------------------

//       poke(c.io.ReadIn(readidx).valid, true)
//       poke(c.io.ReadIn(readidx).bits.address, 34)
//       poke(c.io.ReadIn(readidx).bits.node, 3)
//       // poke(c.io.ReadIn(readidx).bits.mask, 3)

//       //-------------------------------------------
//       poke(c.io.ReadIn(0).valid, true)
//       poke(c.io.ReadIn(0).bits.address, 43)
//       poke(c.io.ReadIn(0).bits.node, 0)
//       // poke(c.io.ReadIn(0).bits.mask, 0)
//     }
//     else {
//       poke(c.io.ReadIn(readidx).valid, false)
//       poke(c.io.ReadIn(0).valid, false)
//       poke(c.io.ReadIn(1).valid, false)
//       poke(c.io.ReadIn(2).valid, false)

//     }

//     //    //TODO Create separate Test case for Demux
//     //    // To test Demux
//     //    if(t==1) {
//     //      poke(c.io.en,true)
//     //      poke(c.io.sel,readidx)
//     //      poke(c.io.input.data, 13)
//     //      poke(c.io.input.valid, 1)
//     //    }
//     //    else {
//     //      poke(c.io.en, false)
//     //    }

//     printf(s"t: ${t} ---------------------------- \n")
//     printf(s"t: ${t}  io.ReadOut: ${peek(c.io.ReadOut(readidx))} chosen: ${readidx} \n")
//     printf(s"t: ${t}  io.ReadOut: ${peek(c.io.ReadOut(0))} chosen: 0 \n")
// //    printf(s"t: ${t}  io.ReadIn: ${peek(c.io.ReadIn(readidx))} chosen: ${readidx} \n")
    step(1)

  }

}


class MemoryControllerTester extends  FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  it should "Memory Controller tester" in {
    chisel3.iotesters.Driver(() => new MemoryController(NumOps=2)(p)) {
      c => new MemoryControllerTests(c)
    } should be(true)
  }
}
