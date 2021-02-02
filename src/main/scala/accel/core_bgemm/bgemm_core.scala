package dandelion.accel

import chisel3._
import chisel3.util._
import utility.UniformPrintfs
import chipsalliance.rocketchip.config._
import dandelion.config._
import dandelion.interfaces._
import chipsalliance.rocketchip.config._
import dandelion.config._
import dandelion.memory.cache.{CacheCPUIO,ReferenceCache}
import dandelion.generator.bgemmRootDF

/**
  * The Core class contains the dataflow logic for the accelerator.
  * This particular core file implements a simple memory test routine to
  * validate the register interface and the Nasti bus operation on an SoC FPGA.
  *
  * @param p Project parameters. Only xlen is used to specify register and
  *          data bus width.
  * @note io.ctrl  A control register (from SimpleReg block) to start test
  * @note io.addr  A control register containing the physical address for
  *       the test
  * @note io.len   A control register containing the length of the memory
  *       test (number of words)
  * @note io.stat  A status register containing the current state of the test
  * @note io.cache A Read/Write request interface to a memory cache block
  */

abstract class CoreDFIO_edited(cNum : Int, sNum: Int)(implicit val p: Parameters) extends Module 
  with HasAccelParams 
  with UniformPrintfs {
  val io = IO(
    new Bundle {
      val start  = Input(Bool())
      val init   = Input(Bool())
      val ready  = Output(Bool())
      val done   = Output(Bool())
      val ctrl = Input(Vec(cNum,UInt(xlen.W)))
      val stat = Output(Vec(sNum,UInt(xlen.W)))

      // val temp = new DataBundle()
      
      // val ctrl   = Vec(cNum,Flipped(Decoupled(new DataBundle())))
      // val stat   = Vec(sNum,Decoupled(new DataBundle()))
      val cache = Flipped(new CacheCPUIO)
    }
  )
}

abstract class CoreT_edited(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreDFIO_edited(cNum,sNum)(p) {
}

class Core_edited(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreT_edited(cNum,sNum)(p) {

  // Core initial register
  val (sIdle :: sCompute :: sFinish :: sFlush :: sDone :: Nil) = Enum(5)
  val state = RegInit(init = sIdle)
  val counter = RegInit(0.U(xlen.W))
  val counter2 = RegInit(0.U(xlen.W)) //counter register
  val a = RegInit(0.U(xlen.W))
  val b = RegInit(0.U(xlen.W))
  val c = RegInit(0.U(xlen.W))
  val task_id = RegInit(0.U(tlen.W))
  val inValid = RegInit(false.B)

  //io.ctrl
  a := io.ctrl(0)
  b := io.ctrl(1)
  c := io.ctrl(2)

  //io.stat
  io.stat(0) := counter
  io.stat(1) := counter2
  // io.stat(1) := result

  // io.done and io.ready (reflect state machine)
  io.done := (state === sDone)
  io.ready := (state === sIdle)

  // Dataflow
  val mainDataflow = Module(new bgemmRootDF(PtrsIn = List(16,16,16), ValsIn = List(), Returns = List(), NumChild = 4))

  mainDataflow.io.in.bits.enable.taskID := 0.U
  mainDataflow.io.in.bits.enable.control := true.B

  // Connect main dataflow and input  

  mainDataflow.io.in.bits.dataPtrs("field0").data := a
  mainDataflow.io.in.bits.dataPtrs("field0").taskID := task_id
  mainDataflow.io.in.bits.dataPtrs("field0").predicate := true.B

  mainDataflow.io.in.bits.dataPtrs("field1").data := b
  mainDataflow.io.in.bits.dataPtrs("field1").taskID := task_id
  mainDataflow.io.in.bits.dataPtrs("field1").predicate := true.B

  mainDataflow.io.in.bits.dataPtrs("field2").data := c
  mainDataflow.io.in.bits.dataPtrs("field2").taskID := task_id
  mainDataflow.io.in.bits.dataPtrs("field2").predicate := true.B


  mainDataflow.io.in.valid := inValid
  mainDataflow.io.out.ready := true.B //keep it ready to recieve output from DF

  // // Connect CacheArb to cache
  io.cache.req <> mainDataflow.io.MemReq
  mainDataflow.io.MemResp <> io.cache.resp
  io.cache.flush := false.B
  io.cache.abort := false.B

  //State machine
  switch(state) {
   // Idle
    is(sIdle) {  
      when(io.start && mainDataflow.io.in.ready){ // in.ready is not ready after init
        counter := 0.U
        counter2 := 0.U
        inValid := true.B
        state := sCompute
      }
    }
    // Compute
    is(sCompute) {
      //Count Clock Cycle
      counter := Mux(counter === "hffff".U, 0.U, counter + 1.U)
      counter2 := Mux(counter === "hffff".U, counter2 + 1.U , counter2)
      inValid := false.B

      // Result is valid only when receive valid signal
      when (mainDataflow.io.out.valid){ 
        io.cache.flush := true.B
        state := sFinish
      }
    }
    is(sFinish){
      io.cache.flush := false.B
      when(io.cache.flush_done){
        state := sDone
      }
    }
    // Done
    is(sDone) {
      when(io.init) {
        state := sIdle
      }
    }
  }

}

// import java.io.{File, FileWriter}

// object core_edited_Main extends App {
//   val dir = new File("RTL/core_edited_Main");
//   dir.mkdirs
//   implicit val p = new WithAccelConfig ++ new WithTestConfig
//   val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Core_edited(3,1)))

//   val verilogFile = new File(dir, s"${chirrtl.main}.v")
//   val verilogWriter = new FileWriter(verilogFile)
//   val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
//   val compiledStuff = compileResult.getEmittedCircuit
//   verilogWriter.write(compiledStuff.value)
//   verilogWriter.close()
// }