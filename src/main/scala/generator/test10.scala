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

object Data_test10_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_for_cond_pred = Map(
    "br0" -> 0,
    "br13" -> 1
  )


  val bb_for_inc_pred = Map(
    "br11" -> 0
  )


  val bb_for_body_pred = Map(
    "br3" -> 0
  )


  val bb_for_end_pred = Map(
    "br3" -> 0
  )


  val br0_brn_bb = Map(
    "bb_for_cond" -> 0
  )


  val br3_brn_bb = Map(
    "bb_for_body" -> 0,
    "bb_for_end" -> 1
  )


  val br11_brn_bb = Map(
    "bb_for_inc" -> 0
  )


  val br13_brn_bb = Map(
    "bb_for_cond" -> 0
  )


  val bb_entry_activate = Map(
    "br0" -> 0
  )


  val bb_for_cond_activate = Map(
    "phi1" -> 0,
    "icmp2" -> 1,
    "br3" -> 2
  )


  val bb_for_body_activate = Map(
    "getelementptr4" -> 0,
    "load5" -> 1,
    "getelementptr6" -> 2,
    "load7" -> 3,
    "add8" -> 4,
    "getelementptr9" -> 5,
    "store10" -> 6,
    "br11" -> 7
  )


  val bb_for_inc_activate = Map(
    "add12" -> 0,
    "br13" -> 1
  )


  val bb_for_end_activate = Map(
    "ret14" -> 0
  )


  val phi1_phi_in = Map(
    "const_0" -> 0,
    "add12" -> 1
  )


  //  %i.0 = phi i32 [ 0, %entry ], [ %inc, %for.inc ], !UID !11, !ScalaLabel !12
  val phi1_in = Map(
    "add12" -> 0
  )


  //  %cmp = icmp ult i32 %i.0, 5, !UID !13, !ScalaLabel !14
  val icmp2_in = Map(
    "phi1" -> 0
  )


  //  br i1 %cmp, label %for.body, label %for.end, !UID !15, !BB_UID !16, !ScalaLabel !17
  val br3_in = Map(
    "icmp2" -> 0
  )


  //  %arrayidx = getelementptr inbounds i32, i32* %a, i32 %i.0, !UID !18, !ScalaLabel !19
  val getelementptr4_in = Map(
    "field0" -> 0,
    "phi1" -> 1
  )


  //  %0 = load i32, i32* %arrayidx, align 4, !UID !20, !ScalaLabel !21
  val load5_in = Map(
    "getelementptr4" -> 0
  )


  //  %arrayidx1 = getelementptr inbounds i32, i32* %b, i32 %i.0, !UID !22, !ScalaLabel !23
  val getelementptr6_in = Map(
    "field1" -> 0,
    "phi1" -> 2
  )


  //  %1 = load i32, i32* %arrayidx1, align 4, !UID !24, !ScalaLabel !25
  val load7_in = Map(
    "getelementptr6" -> 0
  )


  //  %add = add i32 %0, %1, !UID !26, !ScalaLabel !27
  val add8_in = Map(
    "load5" -> 0,
    "load7" -> 0
  )


  //  %arrayidx2 = getelementptr inbounds i32, i32* %c, i32 %i.0, !UID !28, !ScalaLabel !29
  val getelementptr9_in = Map(
    "field2" -> 0,
    "phi1" -> 3
  )


  //  store i32 %add, i32* %arrayidx2, align 4, !UID !30, !ScalaLabel !31
  val store10_in = Map(
    "add8" -> 0,
    "getelementptr9" -> 0
  )


  //  %inc = add i32 %i.0, 1, !UID !35, !ScalaLabel !36
  val add12_in = Map(
    "phi1" -> 4
  )


  //  ret i32 1, !UID !49, !BB_UID !50, !ScalaLabel !51
  val ret14_in = Map(

  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class test10DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32,32,32))))
    val CacheResp = Flipped(Valid(new CacheRespT))
    val CacheReq = Decoupled(new CacheReq)
    val out = Decoupled(new Call(List(32)))
  })
}




  /* ================================================================== *
   *                   PRINTING MODULE DEFINITION                       *
   * ================================================================== */


class test10DF(implicit p: Parameters) extends test10DFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */


	val StackPointer = Module(new Stack(NumOps = 1))

	val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=2,NWrites=1)
		            (WControl=new WriteMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2)))

	val CacheMem = Module(new UnifiedController(ID=0,Size=32,NReads=2,NWrites=1)
		            (WControl=new WriteMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RWArbiter=new ReadWriteArbiter()))

  io.CacheReq <> CacheMem.io.CacheReq
  CacheMem.io.CacheResp <> io.CacheResp

  val InputSplitter = Module(new SplitCall(List(32,32,32)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  val loop_L_4_liveIN_0 = Module(new LiveInNode(NumOuts = 1, ID = 0))
  val loop_L_4_liveIN_1 = Module(new LiveInNode(NumOuts = 1, ID = 0))
  val loop_L_4_liveIN_2 = Module(new LiveInNode(NumOuts = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_for_cond = Module(new BasicBlockLoopHeadNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 1))

  val bb_for_body = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 8, BID = 2))

  val bb_for_inc = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 3))

  val bb_for_end = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 4, BID = 4))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  br label %for.cond, !UID !8, !BB_UID !9, !ScalaLabel !10
  val br0 = Module (new UBranchNode(ID = 0))

  // [BasicBlock]  for.cond:

  //  %i.0 = phi i32 [ 0, %entry ], [ %inc, %for.inc ], !UID !11, !ScalaLabel !12
  val phi1 = Module (new PhiNode(NumInputs = 2, NumOuts = 5, ID = 1))


  //  %cmp = icmp ult i32 %i.0, 5, !UID !13, !ScalaLabel !14
  val icmp2 = Module (new IcmpNode(NumOuts = 1, ID = 2, opCode = "ULT")(sign=false))


  //  br i1 %cmp, label %for.body, label %for.end, !UID !15, !BB_UID !16, !ScalaLabel !17
  val br3 = Module (new CBranchNode(ID = 3))

  // [BasicBlock]  for.body:

  //  %arrayidx = getelementptr inbounds i32, i32* %a, i32 %i.0, !UID !18, !ScalaLabel !19
  val getelementptr4 = Module (new GepOneNode(NumOuts = 1, ID = 4)(numByte1 = 1))


  //  %0 = load i32, i32* %arrayidx, align 4, !UID !20, !ScalaLabel !21
  val load5 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=5,RouteID=0))


  //  %arrayidx1 = getelementptr inbounds i32, i32* %b, i32 %i.0, !UID !22, !ScalaLabel !23
  val getelementptr6 = Module (new GepOneNode(NumOuts = 1, ID = 6)(numByte1 = 1))


  //  %1 = load i32, i32* %arrayidx1, align 4, !UID !24, !ScalaLabel !25
  val load7 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=7,RouteID=1))


  //  %add = add i32 %0, %1, !UID !26, !ScalaLabel !27
  val add8 = Module (new ComputeNode(NumOuts = 1, ID = 8, opCode = "add")(sign=false))


  //  %arrayidx2 = getelementptr inbounds i32, i32* %c, i32 %i.0, !UID !28, !ScalaLabel !29
  val getelementptr9 = Module (new GepOneNode(NumOuts = 1, ID = 9)(numByte1 = 1))


  //  store i32 %add, i32* %arrayidx2, align 4, !UID !30, !ScalaLabel !31
  val store10 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=10,RouteID=0))


  //  br label %for.inc, !UID !32, !BB_UID !33, !ScalaLabel !34
  val br11 = Module (new UBranchNode(ID = 11))

  // [BasicBlock]  for.inc:

  //  %inc = add i32 %i.0, 1, !UID !35, !ScalaLabel !36
  val add12 = Module (new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign=false))


  //  br label %for.cond, !llvm.loop !37, !UID !46, !BB_UID !47, !ScalaLabel !48
  val br13 = Module (new UBranchNode(ID = 13))

  // [BasicBlock]  for.end:

  //  ret i32 1, !UID !49, !BB_UID !50, !ScalaLabel !51
  val ret14 = Module(new RetNode(NumPredIn=1, retTypes=List(32), ID=14))





  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_test10_FlowParam



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

  //Connecting br0 to bb_for_cond
  bb_for_cond.io.predicateIn(param.bb_for_cond_pred("br0")) <> br0.io.Out(param.br0_brn_bb("bb_for_cond"))


  //Connecting br3 to bb_for_body
  bb_for_body.io.predicateIn <> br3.io.Out(param.br3_brn_bb("bb_for_body"))


  //Connecting br3 to bb_for_end
  bb_for_end.io.predicateIn <> br3.io.Out(param.br3_brn_bb("bb_for_end"))


  //Connecting br11 to bb_for_inc
  bb_for_inc.io.predicateIn <> br11.io.Out(param.br11_brn_bb("bb_for_inc"))


  //Connecting br13 to bb_for_cond
  bb_for_cond.io.predicateIn(param.bb_for_cond_pred("br13")) <> br13.io.Out(param.br13_brn_bb("bb_for_cond"))




  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

  br0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("br0"))



  phi1.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("phi1"))

  icmp2.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("icmp2"))

  br3.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("br3"))



  getelementptr4.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("getelementptr4"))

  load5.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("load5"))

  getelementptr6.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("getelementptr6"))

  load7.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("load7"))

  add8.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("add8"))

  getelementptr9.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("getelementptr9"))

  store10.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("store10"))

  br11.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("br11"))



  add12.io.enable <> bb_for_inc.io.Out(param.bb_for_inc_activate("add12"))

  br13.io.enable <> bb_for_inc.io.Out(param.bb_for_inc_activate("br13"))



  ret14.io.enable <> bb_for_end.io.Out(param.bb_for_end_activate("ret14"))

  loop_L_4_liveIN_0.io.enable <> bb_for_end.io.Out(1)
  loop_L_4_liveIN_1.io.enable <> bb_for_end.io.Out(2)
  loop_L_4_liveIN_2.io.enable <> bb_for_end.io.Out(3)






  /* ================================================================== *
   *                   CONNECTING LOOPHEADERS                           *
   * ================================================================== */


  // Connecting function argument to the loop header
  //i32* %a
  loop_L_4_liveIN_0.io.InData <> InputSplitter.io.Out.data("field0")

  // Connecting function argument to the loop header
  //i32* %b
  loop_L_4_liveIN_1.io.InData <> InputSplitter.io.Out.data("field1")

  // Connecting function argument to the loop header
  //i32* %c
  loop_L_4_liveIN_2.io.InData <> InputSplitter.io.Out.data("field2")



  /* ================================================================== *
   *                   DUMPING PHI NODES                                *
   * ================================================================== */


  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  phi1.io.InData(param.phi1_phi_in("const_0")).bits.data := 0.U
  phi1.io.InData(param.phi1_phi_in("const_0")).bits.predicate := true.B
  phi1.io.InData(param.phi1_phi_in("const_0")).valid := true.B

  phi1.io.InData(param.phi1_phi_in("add12")) <> add12.io.Out(param.phi1_in("add12"))

  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  phi1.io.Mask <> bb_for_cond.io.MaskBB(0)



  /* ================================================================== *
   *                   DUMPING DATAFLOW                                 *
   * ================================================================== */


  /**
    * Connecting Dataflow signals
    */

  // Wiring instructions
  icmp2.io.LeftIO <> phi1.io.Out(param.icmp2_in("phi1"))

  // Wiring constant
  icmp2.io.RightIO.bits.data := 5.U
  icmp2.io.RightIO.bits.predicate := true.B
  icmp2.io.RightIO.valid := true.B

  // Wiring Branch instruction
  br3.io.CmpIO <> icmp2.io.Out(param.br3_in("icmp2"))

  // Wiring GEP instruction to the loop header
  getelementptr4.io.baseAddress <> loop_L_4_liveIN_0.io.Out(param.getelementptr4_in("field0"))

  // Wiring GEP instruction to the parent instruction
  getelementptr4.io.idx1 <> phi1.io.Out(param.getelementptr4_in("phi1"))


  // Wiring Load instruction to the parent instruction
  load5.io.GepAddr <> getelementptr4.io.Out(param.load5_in("getelementptr4"))
  load5.io.memResp <> CacheMem.io.ReadOut(0)
  CacheMem.io.ReadIn(0) <> load5.io.memReq




  // Wiring GEP instruction to the loop header
  getelementptr6.io.baseAddress <> loop_L_4_liveIN_1.io.Out(param.getelementptr6_in("field1"))

  // Wiring GEP instruction to the parent instruction
  getelementptr6.io.idx1 <> phi1.io.Out(param.getelementptr6_in("phi1"))


  // Wiring Load instruction to the parent instruction
  load7.io.GepAddr <> getelementptr6.io.Out(param.load7_in("getelementptr6"))
  load7.io.memResp <> CacheMem.io.ReadOut(1)
  CacheMem.io.ReadIn(1) <> load7.io.memReq




  // Wiring instructions
  add8.io.LeftIO <> load5.io.Out(param.add8_in("load5"))

  // Wiring instructions
  add8.io.RightIO <> load7.io.Out(param.add8_in("load7"))

  // Wiring GEP instruction to the loop header
  getelementptr9.io.baseAddress <> loop_L_4_liveIN_2.io.Out(param.getelementptr9_in("field2"))

  // Wiring GEP instruction to the parent instruction
  getelementptr9.io.idx1 <> phi1.io.Out(param.getelementptr9_in("phi1"))


  store10.io.inData <> add8.io.Out(param.store10_in("add8"))



  // Wiring Store instruction to the parent instruction
  store10.io.GepAddr <> getelementptr9.io.Out(param.store10_in("getelementptr9"))
  store10.io.memResp  <> CacheMem.io.WriteOut(0)
  CacheMem.io.WriteIn(0) <> store10.io.memReq
  store10.io.Out(0).ready := true.B



  // Wiring instructions
  add12.io.LeftIO <> phi1.io.Out(param.add12_in("phi1"))

  // Wiring constant
  add12.io.RightIO.bits.data := 1.U
  add12.io.RightIO.bits.predicate := true.B
  add12.io.RightIO.valid := true.B

  // Wiring return instruction
  ret14.io.predicateIn(0).bits.control := true.B
  ret14.io.predicateIn(0).bits.taskID := 0.U
  ret14.io.predicateIn(0).valid := true.B
  ret14.io.In.data("field0").bits.data := 1.U
  ret14.io.In.data("field0").bits.predicate := true.B
  ret14.io.In.data("field0").valid := true.B
  io.out <> ret14.io.Out


}

import java.io.{File, FileWriter}
object test10Main extends App {
  val dir = new File("RTL/test10") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test10DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
