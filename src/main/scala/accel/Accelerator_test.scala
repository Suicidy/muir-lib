// See LICENSE for license details.

package dandelion.accel

import chisel3._
import chisel3.util._
// import accel.coredf._
import chipsalliance.rocketchip.config._
import dandelion.config._
import dandelion.interfaces._
import dandelion.memory.cache._
import dandelion.interfaces.axi._

abstract class AcceleratorIO_edited(cNum : Int, sNum: Int)(implicit val p: Parameters) extends Module with HasAccelParams with HasAccelShellParams {
  val io = IO(
    new Bundle { 
      // Simple Register AXI4 slave interface
      val h2f = Flipped(new AXIMaster(memParams))

      // cache to BRAM AXI$ master interface
      val bram = new AXIMaster(memParams)
    }
  )
}

class Accelerator_edited(cNum : Int, sNum : Int, coreDF: => CoreT_edited) (implicit p: Parameters)extends AcceleratorIO_edited(cNum, sNum)(p) {

  val regs  = Module(new SimpleReg_edited(cNum, sNum))
  val core  = Module(coreDF)
  val cache = Module(new SimpleCache)

  // Simple register connection
  regs.io.nasti <> io.h2f

  core.io.init  <> regs.io.init
  core.io.start <> regs.io.start
  core.io.ctrl  <> regs.io.ctrl
  regs.io.stat  <> core.io.stat
  regs.io.ready <> core.io.ready
  regs.io.done <> core.io.done

  // Connect core and cache
  core.io.cache <> cache.io.cpu
  io.bram <> cache.io.mem
}
