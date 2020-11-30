package dandelion.fpu

import chisel3._
import chisel3.iotesters._
import org.scalatest.{Matchers, FlatSpec}

import chipsalliance.rocketchip.config._
import dandelion.config._


class SharedFPUTests(c: SharedFPU)(implicit p: Parameters) extends PeekPokeTester(c) {

  poke(c.io.InData(0).bits.RouteID, 0.U)
  poke(c.io.InData(1).bits.RouteID, 1.U)
  poke(c.io.InData(0).bits.data("field0").data, 0x42800000.U)
  poke(c.io.InData(0).bits.data("field1").data, 0x41800000.U)
  poke(c.io.InData(0).bits.data("field2").data, 0.U)
  poke(c.io.InData(0).valid, 1.U)
  // poke(c.io.InData(1).valid, 0.U)
  poke(c.io.InData(1).bits.data("field0").data, 0x42800000.U)
  poke(c.io.InData(1).bits.data("field1").data, 0x42800000.U)
  poke(c.io.InData(1).bits.data("field2").data, 0.U)
  poke(c.io.InData(1).valid, 1.U)

  for( i <- 0 until 100){
    print(s"${peek(c.io.OutData(0).data)}  ${peek(c.io.OutData(0).valid)}\n")
    print(s"${peek(c.io.OutData(1).data)}  ${peek(c.io.OutData(1).valid)}\n")

    print(s"t: ${i}\n -------------------------------------\n")
    step(1)
  }
  // step(100)
  // print(s"${peek(c.io.OutData(0).data)}  ${peek(c.io.OutData(0).valid)}\n")
  // step(100)
  // print(s"${peek(c.io.OutData(1).data)}  ${peek(c.io.OutData(1).valid)}\n")
}


class SharedFPUTester extends FlatSpec with Matchers {
  implicit val p = new WithAccelConfig(DandelionAccelParams(dataLen = 32))
  it should "Memory Controller tester" in {
    chisel3.iotesters.Driver(() => new SharedFPU(NumOps = 2, PipeDepth = 32)(t = p(DandelionConfigKey).fType)) {
      c => new SharedFPUTests(c)
    } should be(true)
  }
}
