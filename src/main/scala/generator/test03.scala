package dandelion.generator

import chisel3._
import dandelion.config._
import dandelion.control._
import dandelion.interfaces._
import dandelion.junctions._
import dandelion.memory._
import dandelion.node._
import util._


/* ================================================================== *
 *                   PRINTING PORTS DEFINITION                        *
 * ================================================================== */


abstract class test03DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test03DF(implicit p: Parameters) extends test03DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */


  //-----------------------------------------------------------------------p
  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 0, NWrites = 2)
  //NumOps = 1 to NumOps = 2
  (WControl = new WriteMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 0, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))
  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp
  //---------------------------------------------------------------------------v


  val InputSplitter = Module(new SplitCallNew(List(3, 3)))
  InputSplitter.io.In <> io.in


  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */
  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 9, BID = 0))

  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %3 = icmp slt i32 %1, %0, !UID !3
  val icmp_0 = Module(new ComputeNode(NumOuts = 2, ID = 0, opCode = "lt")(sign = false))

  //  %4 = select i1 %3, i32 %1, i32 0, !UID !4
  val select_1 = Module(new SelectNode(NumOuts = 1, ID = 1)(fast = false))

  //  %5 = sub nsw i32 %0, %4, !UID !5
  val binaryOp_2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "sub")(sign = false))

  //  %6 = select i1 %3, i32 0, i32 %0, !UID !6
  val select_3 = Module(new SelectNode(NumOuts = 1, ID = 3)(fast = false))

  //  %7 = sub nsw i32 %1, %6, !UID !7
  //-----------------------------pv, what we track, numouts ++d
  val binaryOp_4 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "sub")(sign = false, Debug = true))
  val buf_0 = Module(new DebugBufferNode(ID = 8, RouteID = 1, Bore_ID = 4))

  //  %8 = mul nsw i32 %5, %7, !UID !8
  val binaryOp_5 = Module(new ComputeNode(NumOuts = 1, ID = 5, opCode = "mul")(sign = false))

  //  ret i32 %8, !UID !9, !BB_UID !10
  val ret_6 = Module(new RetNode2(retTypes = List(32), ID = 7))

  //-----------------------------------------------------------------------------------p

  val st_0 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 7, RouteID = 0))
  //new



  /**
    * Debuging states for store node
    */

  val sIdle :: sActive :: Nil = Enum(2)
  val state = RegInit(sIdle)

  switch(state) {
    is(sIdle) {
      when(InputSplitter.io.Out.enable.fire) {
        state := sActive
      }
    }
    is(sActive) {
      when(ret_6.io.Out.fire) {
        state := sIdle
      }
    }

  }

  buf_0.io.Enable := state === sActive


  //------------------------------------------------------------------------------------v

  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */


  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */


  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_0.io.Out(0)

  const1.io.enable <> bb_0.io.Out(1)

  icmp_0.io.enable <> bb_0.io.Out(2)

  select_1.io.enable <> bb_0.io.Out(3)

  binaryOp_2.io.enable <> bb_0.io.Out(4)

  select_3.io.enable <> bb_0.io.Out(5)

  binaryOp_4.io.enable <> bb_0.io.Out(6)

  binaryOp_5.io.enable <> bb_0.io.Out(7)

  ret_6.io.In.enable <> bb_0.io.Out(8)

  //new
  //  buf_0.io.enable.bits := ControlBundle.active()
  //  buf_0.io.enable.valid := true.B
  //  buf_0.io.Out(0).ready := true.B

  //new
  //-----------------------------------------p
  st_0.io.enable.bits := ControlBundle.active()
  st_0.io.enable.valid := true.B
  binaryOp_4.io.DebugEnable.get <> bb_0.io.DebugEnable


  //-----------------------------------------------v

  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */
  //-----------------------------------p

  MemCtrl.io.WriteIn(0) <> st_0.io.memReq
  st_0.io.memResp <> MemCtrl.io.WriteOut(0)
  MemCtrl.io.WriteIn(1) <> buf_0.io.memReq
  buf_0.io.memResp <> MemCtrl.io.WriteOut(1)
  //----------------------------------------------v

  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  select_1.io.InData2 <> const0.io.Out

  select_3.io.InData1 <> const1.io.Out

  select_1.io.Select <> icmp_0.io.Out(0)

  select_3.io.Select <> icmp_0.io.Out(1)

  binaryOp_2.io.RightIO <> select_1.io.Out(0)

  binaryOp_5.io.LeftIO <> binaryOp_2.io.Out(0)

  binaryOp_4.io.RightIO <> select_3.io.Out(0)

  binaryOp_5.io.RightIO <> binaryOp_4.io.Out(0)

  ret_6.io.In.data("field0") <> binaryOp_5.io.Out(0)

  icmp_0.io.RightIO <> InputSplitter.io.Out.data.elements("field0")(0)

  binaryOp_2.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(1)

  select_3.io.InData2 <> InputSplitter.io.Out.data.elements("field0")(2)

  icmp_0.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(0)

  select_1.io.InData1 <> InputSplitter.io.Out.data.elements("field1")(1)

  binaryOp_4.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(2)

  //------------------------------------------p for handshaking debugging

  val data_queue = Queue(binaryOp_4.io.LogCheck.get, 20)
  val addr_queue = Queue(binaryOp_4.io.LogCheckAddr.get, 20)

  data_queue.nodeq()
  addr_queue.nodeq()

  when(st_0.io.inData.ready && data_queue.valid && addr_queue.valid) {
    st_0.io.inData.enq(data_queue.deq().asDataBundle())
    st_0.io.GepAddr.enq(addr_queue.deq().asDataBundle())
  }.otherwise {
    st_0.io.inData.noenq()
    st_0.io.GepAddr.noenq()
  }

  st_0.io.Out(0).ready := true.B

  //------------------------------------------------------------v


  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_6.io.Out

}

import java.io.{File, FileWriter}

object test03Top extends App {
  val dir = new File("RTL/test03Top");
  dir.mkdirs
  implicit val p = Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test03DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}








