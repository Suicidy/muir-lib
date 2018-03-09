package dataflow

import chisel3._
import chisel3.util._
import chisel3.Module
import chisel3.testers._
import chisel3.iotesters._
import org.scalatest.{FlatSpec, Matchers}
import muxes._
import config._
import control._
import util._
import interfaces._
import regfile._
import memory._
import stack._
import arbiters._
import loop._
import accel._
import node._
import junctions._


/**
  * This Object should be initialized at the first step
  * It contains all the transformation from indices to their module's name
  */

object Data_test01_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_entry_activate = Map(
    "mul0" -> 0,
    "ret1" -> 1
  )


  //  %mul = mul i32 %a, %b, !UID !8, !ScalaLabel !9
  val mul0_in = Map(
    "field0" -> 0,
    "field1" -> 0
  )


  //  ret i32 %mul, !UID !10, !BB_UID !11, !ScalaLabel !12
  val ret1_in = Map(
    "mul0" -> 0
  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class test01DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32,32))))
    val CacheResp = Flipped(Valid(new CacheRespT))
    val CacheReq = Decoupled(new CacheReq)
    val out = Decoupled(new Call(List(32)))
  })
}




  /* ================================================================== *
   *                   PRINTING MODULE DEFINITION                       *
   * ================================================================== */


class test01DF(implicit p: Parameters) extends test01DFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */


	val StackPointer = Module(new Stack(NumOps = 1))

	val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=2,NWrites=2)
		            (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2)))

	val CacheMem = Module(new UnifiedController(ID=0,Size=32,NReads=2,NWrites=2)
		            (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RWArbiter=new ReadWriteArbiter()))

  io.CacheReq <> CacheMem.io.CacheReq
  CacheMem.io.CacheResp <> io.CacheResp

  val InputSplitter = Module(new SplitCall(List(32,32)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  //Function doesn't have any loop


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 0))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %mul = mul i32 %a, %b, !UID !8, !ScalaLabel !9
  val mul0 = Module (new ComputeNode(NumOuts = 1, ID = 0, opCode = "mul")(sign=false))


  //  ret i32 %mul, !UID !10, !BB_UID !11, !ScalaLabel !12
  val ret1 = Module(new RetNode(NumPredIn=1, retTypes=List(32), ID=1))







  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_test01_FlowParam



  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO PREDICATE INSTRUCTIONS*
   * ================================================================== */


  /**
     * Connecting basic blocks to predicate instructions
     */


  bb_entry.io.predicateIn <> InputSplitter.io.Out.enable

  /**
    * Connecting basic blocks to predicate instructions
    */


  // There is no branch instruction




  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

  mul0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("mul0"))

  ret1.io.enable <> bb_entry.io.Out(param.bb_entry_activate("ret1"))





  /* ================================================================== *
   *                   CONNECTING LOOPHEADERS                           *
   * ================================================================== */


  //Function doesn't have any for loop


  /* ================================================================== *
   *                   DUMPING PHI NODES                                *
   * ================================================================== */


  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  /**
    * Connecting PHI Masks
    */
  //Connect PHI node
  // There is no PHI node


  /* ================================================================== *
   *                   DUMPING DATAFLOW                                 *
   * ================================================================== */


  /**
    * Connecting Dataflow signals
    */

  // Wiring Binary instruction to the function argument
  mul0.io.LeftIO <> InputSplitter.io.Out.data("field0")

  // Wiring Binary instruction to the function argument
  mul0.io.RightIO <> InputSplitter.io.Out.data("field1")

  // Wiring return instruction
  ret1.io.predicateIn(0).bits.control := true.B
  ret1.io.predicateIn(0).bits.taskID := 0.U
  ret1.io.predicateIn(0).valid := true.B
  ret1.io.In.data("field0") <> mul0.io.Out(param.ret1_in("mul0"))
  io.out <> ret1.io.Out


}

import java.io.{File, FileWriter}
object test01Main extends App {
  val dir = new File("RTL/test01") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test01DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
