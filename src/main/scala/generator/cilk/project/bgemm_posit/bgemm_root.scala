package dandelion.generator 

import chipsalliance.rocketchip.config._
import chisel3._
import dandelion.accel._
import dandelion.memory._
import dandelion.config._
import dandelion.concurrent._
class bgemmRoot_positDF(PtrsIn : Seq[Int] = List (32, 32, 32), ValsIn : Seq[Int] = List(), Returns: Seq[Int] = List(), NumChild: Int = 1)
                  (implicit p: Parameters) extends DandelionAccelDCRModule(PtrsIn, ValsIn, Returns) {
  /**
    * Local memories
    */

  /**
    * Kernel Modules
    */
  val NumTiles = NumChild

  val memory_arbiter = Module(new MemArbiter(NumTiles+1))

  val cilk_for_tiles = for (i <- 0 until NumTiles) yield {
    val bgemm_detach1 = Module(new bgemm_detach1_positDF(PtrsIn = List(32, 32, 32), ValsIn = List(32, 32, 32), Returns = List()))
    bgemm_detach1
  }

  /**
    * Kernel Modules
    */
  val bgemm =  Module(new bgemm_positDF(PtrsIn = List(32, 32, 32), ValsIn = List(), Returns = List()))

  val TC = Module(new TaskController(List(32, 32, 32, 32, 32, 32), List(), 1, numChild = NumTiles))

    // Merge the memory interfaces and connect to the stack memory
  for (i <- 0 until NumTiles) {
    // Connect to stack memory interface
    memory_arbiter.io.cpu.MemReq(i) <> cilk_for_tiles(i).io.MemReq
    cilk_for_tiles(i).io.MemResp <> memory_arbiter.io.cpu.MemResp(i)


    // Connect to task controller
    cilk_for_tiles(i).io.in.bits.dataPtrs.elements("field0") <> TC.io.childOut(i).bits.data.elements("field0") //dataPtrs(0) = m1
    cilk_for_tiles(i).io.in.bits.dataPtrs.elements("field1") <> TC.io.childOut(i).bits.data.elements("field1") //dataPtrs(1) = m2
    cilk_for_tiles(i).io.in.bits.dataPtrs.elements("field2") <> TC.io.childOut(i).bits.data.elements("field2") //dataPtrs(2) = prod

    cilk_for_tiles(i).io.in.bits.dataVals.elements("field0") <> TC.io.childOut(i).bits.data.elements("field5") // loop i
    cilk_for_tiles(i).io.in.bits.dataVals.elements("field1") <> TC.io.childOut(i).bits.data.elements("field4") // loop kk
    cilk_for_tiles(i).io.in.bits.dataVals.elements("field2") <> TC.io.childOut(i).bits.data.elements("field3") // loop jj
    
    // bug fixing
    TC.io.childOut(i).ready <>cilk_for_tiles(i).io.in.ready
    cilk_for_tiles(i).io.in.valid <> TC.io.childOut(i).valid
    cilk_for_tiles(i).io.in.bits.enable <> TC.io.childOut(i).bits.enable

    TC.io.childIn(i) <> cilk_for_tiles(i).io.out
  }
  // parent
  TC.io.parentIn(0) <> bgemm.io.call_27_out_io
  bgemm.io.call_27_in_io <> TC.io.parentOut(0)

  memory_arbiter.io.cpu.MemReq(NumTiles) <> bgemm.io.MemReq
  bgemm.io.MemResp <> memory_arbiter.io.cpu.MemResp(NumTiles)
  
  bgemm.io.in <> io.in
  io.out <> bgemm.io.out


  io.MemReq <> memory_arbiter.io.cache.MemReq
  memory_arbiter.io.cache.MemResp <> io.MemResp

}

// import java.io.{File, FileWriter}

// object bgemmedited_posit extends App {
//   val dir = new File("RTL/bgemmedited_posit");
//   dir.mkdirs
//   implicit val p = new WithAccelConfig ++ new WithTestConfig
//   val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new bgemmRoot_positDF()))

//   val verilogFile = new File(dir, s"${chirrtl.main}.v")
//   val verilogWriter = new FileWriter(verilogFile)
//   val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
//   val compiledStuff = compileResult.getEmittedCircuit
//   verilogWriter.write(compiledStuff.value)
//   verilogWriter.close()
}
