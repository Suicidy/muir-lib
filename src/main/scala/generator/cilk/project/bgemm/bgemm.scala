package dandelion.generator

import chipsalliance.rocketchip.config._
import chisel3._
import chisel3.util._
import chisel3.Module._
import chisel3.testers._
import chisel3.iotesters._
import dandelion.accel._
import dandelion.arbiters._
import dandelion.config._
import dandelion.control._
import dandelion.fpu._
import dandelion.interfaces._
import dandelion.junctions._
import dandelion.loop._
import dandelion.memory._
import dandelion.memory.stack._
import dandelion.node._
import muxes._
import org.scalatest._
import regfile._
import util._

// add detach node package
import dandelion.concurrent._


abstract class bgemmDFIO[T <: Data](val PtrsIn: Seq[Int],
                                    val ValsIn: Seq[Int],
                                    val RetsOut: Seq[Int])(implicit val p: Parameters) extends Module with HasAccelParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new CallDCR(PtrsIn, ValsIn)))
    
    // val call_9_out = Decoupled(new Call(List(32, 32, 32, 32)))
    // val call_9_in = Flipped(Decoupled(new Call(List())))

    // val call_27_out_io = Decoupled(new CallDCR(ptrsArgTypes = List(32,32,32), valsArgTypes = List(32,32,32)))
    val call_27_out_io = Decoupled(new Call(List(32, 32, 32, 32, 32, 32)))
    val call_27_in_io = Flipped(Decoupled(new Call(List())))

    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)

    val out = Decoupled(new Call(RetsOut))
  })
}


// class bgemmDF(PtrsIn: Seq[Int] = List(32, 32, 32), ValsIn: Seq[Int] = List(), Returns: Seq[Int] = List())
// 			(implicit p: Parameters) extends DandelionAccelDCRModule(PtrsIn, ValsIn, Returns){
  class bgemmDF(PtrsIn: Seq[Int] = List(32, 32, 32), ValsIn: Seq[Int] = List(), Returns: Seq[Int] = List())
			(implicit p: Parameters) extends bgemmDFIO(PtrsIn, ValsIn, Returns){
  /**
    * Call Interfaces
    */
  // val call_27_out_io = IO(Decoupled(new CallDCR(ptrsArgTypes = List(), valsArgTypes = List(32))))
  // val call_27_in_io = IO(Flipped(Decoupled(new Call(List()))))

  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val ArgSplitter = Module(new SplitCallDCR(ptrsArgTypes = List(1, 1, 1), valsArgTypes = List()))
  ArgSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_for_cond_cleanup3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 3))

  val bb_loopkk5 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 5))

  val bb_for_cond_cleanup38 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 8))

  val bb_loopi12 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 12))
  
  // add block for sync node
  val bb_pfor_cond_cleanup28 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 28))

  val bb_pfor_detach15 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 15))

  val bb_pfor_inc18 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 18))

  val bb_sync_continue22 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 22))

  val bb_offload_loopk26 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 26))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %loopkk, !UID !4, !BB_UID !5
  // val br_2 = Module(new UBranchNode(NumPredOps=1, ID = 2))
  val br_2 = Module(new UBranchNode(ID = 2))

  //  ret void, !UID !6, !BB_UID !7
  val ret_4 = Module(new RetNode2(retTypes = List(), ID = 4))

  //  %jj.070 = phi i32 [ 0, %entry ], [ %add37, %for.cond.cleanup3 ], !UID !8
  val phijj_0706 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 6, Res = true))

  //  br label %loopi, !UID !9, !BB_UID !10
  val br_7 = Module(new UBranchNode(ID = 7))

  //  %add37 = add nuw nsw i32 %jj.070, 2, !UID !11
  val binaryOp_add379 = Module(new ComputeNode(NumOuts = 2, ID = 9, opCode = "add")(sign = false, Debug = false))

  //  %cmp = icmp ult i32 %add37, 4, !UID !12
  val icmp_cmp10 = Module(new ComputeNode(NumOuts = 1, ID = 10, opCode = "slt")(sign = false, Debug = false))

  //  br i1 %cmp, label %loopkk, label %for.cond.cleanup, !UID !13, !BB_UID !14
  val br_11 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 11))

  //  %kk.069 = phi i32 [ 0, %loopkk ], [ %add34, %sync.continue ], !UID !15
  val phikk_06913 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 13, Res = true))

  //  br label %pfor.detach, !UID !16, !BB_UID !17
  val br_14 = Module(new UBranchNode(ID = 14))

  //  %__begin.068 = phi i32 [ 0, %loopi ], [ %inc32, %pfor.inc ], !UID !18
  val phi__begin_06816 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 16, Res = true))

  //  detach within %syncreg, label %offload.loopk, label %pfor.inc, !UID !19, !BB_UID !20
  val detach_11 = Module(new Detach(ID = 11))

  //  %inc32 = add nuw nsw i32 %__begin.068, 1, !UID !21
  val binaryOp_inc3219 = Module(new ComputeNode(NumOuts = 2, ID = 19, opCode = "add")(sign = false, Debug = false))

  //  %exitcond72 = icmp eq i32 %inc32, 4, !UID !22
  val icmp_exitcond7220 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "eq")(sign = false, Debug = false))

  //  br i1 %exitcond72, label %sync.continue, label %pfor.detach, !llvm.loop !23, !UID !25, !BB_UID !26
  val br_21 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 21))

  //  %add34 = add nuw nsw i32 %kk.069, 2, !UID !27
  val binaryOp_add3423 = Module(new ComputeNode(NumOuts = 2, ID = 23, opCode = "add")(sign = false, Debug = false))

  //  %cmp2 = icmp ult i32 %add34, 4, !UID !28
  val icmp_cmp224 = Module(new ComputeNode(NumOuts = 1, ID = 24, opCode = "slt")(sign = false, Debug = false))

  //  br i1 %cmp2, label %loopi, label %for.cond.cleanup3, !UID !29, !BB_UID !30
  val br_25 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 25))
  
  
  // addnode
  //  call 27
  val call_27_out = Module(new CallOutNode(ID = 27, NumSuccOps = 0, argTypes = List(32,32,32,32,32,32)))

  val call_27_in = Module(new CallInNode(ID = 27, argTypes = List()))

  //  reattach 19
  val reattach_19 = Module(new Reattach(NumPredOps= 1, ID = 19))

  // sync node
  val sync_28 = Module(new SyncTC(ID = 28, NumInc=1, NumDec=1, NumOuts=1))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 2
  val const1 = Module(new ConstFastNode(value = 8, ID = 1))

  //i32 4
  val const2 = Module(new ConstFastNode(value = 16, ID = 2))

  //i32 0
  val const3 = Module(new ConstFastNode(value = 0, ID = 3))

  //i32 0
  val const4 = Module(new ConstFastNode(value = 0, ID = 4))

  //i32 1
  val const5 = Module(new ConstFastNode(value = 1, ID = 5))

  //i32 4
  val const6 = Module(new ConstFastNode(value = 16, ID = 6))

  //i32 2
  val const7 = Module(new ConstFastNode(value = 8, ID = 7))

  //i32 4
  val const8 = Module(new ConstFastNode(value = 16, ID = 8))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry1.io.predicateIn(0) <> ArgSplitter.io.Out.enable

  bb_pfor_inc18.io.predicateIn(0) <> detach_11.io.Out(0)

  bb_offload_loopk26.io.predicateIn(0) <> detach_11.io.Out(1)

  bb_sync_continue22.io.predicateIn(0) <> sync_28.io.Out(0)


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_for_cond_cleanup3.io.predicateIn(0) <> Loop_2.io.loopExit(0)

  bb_loopkk5.io.predicateIn(1) <> Loop_2.io.activate_loop_start

  bb_loopkk5.io.predicateIn(0) <> Loop_2.io.activate_loop_back

  bb_for_cond_cleanup38.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_loopi12.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_loopi12.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_pfor_detach15.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_pfor_detach15.io.predicateIn(0) <> Loop_0.io.activate_loop_back


  // add sync node and block
  // bb_sync_continue22.io.predicateIn(0) <> Loop_0.io.loopExit(0)
  bb_pfor_cond_cleanup28.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_28.io.incIn(0) <> detach_11.io.Out(2)

  sync_28.io.decIn(0) <> reattach_19.io.Out(0)

  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_14.io.Out(0)

  Loop_0.io.loopBack(0) <> br_21.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_21.io.TrueOutput(0)

  Loop_1.io.enable <> br_7.io.Out(0)

  Loop_1.io.loopBack(0) <> br_25.io.TrueOutput(0)

  Loop_1.io.loopFinish(0) <> br_25.io.FalseOutput(0)

  Loop_2.io.enable <> br_2.io.Out(0)

  Loop_2.io.loopBack(0) <> br_11.io.TrueOutput(0)

  Loop_2.io.loopFinish(0) <> br_11.io.FalseOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> phikk_06913.io.Out(0)

  Loop_0.io.InLiveIn(1) <> Loop_1.io.OutLiveIn.elements("field1")(0) //ArgSplitter.io.Out.dataPtrs.elements("field0")(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field2")(0) //ArgSplitter.io.Out.dataPtrs.elements("field1")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field0")(0) //phijj_0706.io.Out(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field3")(0) // ArgSplitter.io.Out.dataPtrs.elements("field2")(0)

  Loop_1.io.InLiveIn(0) <> phijj_0706.io.Out(0)

  Loop_1.io.InLiveIn(1) <> Loop_2.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(2) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(3) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_2.io.InLiveIn(0) <> ArgSplitter.io.Out.dataPtrs.elements("field0")(0)

  Loop_2.io.InLiveIn(1) <> ArgSplitter.io.Out.dataPtrs.elements("field1")(0)

  Loop_2.io.InLiveIn(2) <> ArgSplitter.io.Out.dataPtrs.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  // // call_27_out.io.inPtrs.elements("field1") <> Loop_0.io.OutLiveIn.elements("field0")(0)
  // call_27_out.io.In.elements("field1") <> Loop_0.io.OutLiveIn.elements("field0")(0)

  // // call_27_out.io.inPtrs.elements("field2") <> Loop_0.io.OutLiveIn.elements("field1")(0)
  // call_27_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field1")(0)

  // call_27_out.io.inPtrs.elements("field4") <> Loop_0.io.OutLiveIn.elements("field2")(0)

  // call_27_out.io.inPtrs.elements("field3") <> Loop_0.io.OutLiveIn.elements("field3")(0)

  // call_27_out.io.inPtrs.elements("field5") <> Loop_0.io.OutLiveIn.elements("field4")(0)



// new call in call out connection

  // for dataPointer
  call_27_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field1")(0)
  call_27_out.io.In.elements("field1") <> Loop_0.io.OutLiveIn.elements("field2")(0)
  call_27_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field4")(0)

  // for valPointer
  call_27_out.io.In.elements("field3") <> Loop_0.io.OutLiveIn.elements("field3")(0) // loop jj
  call_27_out.io.In.elements("field4") <> Loop_0.io.OutLiveIn.elements("field0")(0) // loop kk
  call_27_out.io.In.elements("field5") <> phi__begin_06816.io.Out(1) // loop cilk i
  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc3219.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_add3423.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_add379.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_06816.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phikk_06913.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phijj_0706.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_2.io.enable <> bb_entry1.io.Out(0)


  ret_4.io.In.enable <> bb_for_cond_cleanup3.io.Out(0)


  const0.io.enable <> bb_loopkk5.io.Out(0)

  phijj_0706.io.enable <> bb_loopkk5.io.Out(1)


  br_7.io.enable <> bb_loopkk5.io.Out(2)


  const1.io.enable <> bb_for_cond_cleanup38.io.Out(0)

  const2.io.enable <> bb_for_cond_cleanup38.io.Out(1)

  binaryOp_add379.io.enable <> bb_for_cond_cleanup38.io.Out(2)


  icmp_cmp10.io.enable <> bb_for_cond_cleanup38.io.Out(3)


  br_11.io.enable <> bb_for_cond_cleanup38.io.Out(4)


  const3.io.enable <> bb_loopi12.io.Out(0)

  phikk_06913.io.enable <> bb_loopi12.io.Out(1)


  br_14.io.enable <> bb_loopi12.io.Out(2)


  const4.io.enable <> bb_pfor_detach15.io.Out(0)

  phi__begin_06816.io.enable <> bb_pfor_detach15.io.Out(1)


  detach_11.io.enable <> bb_pfor_detach15.io.Out(2)


  const5.io.enable <> bb_pfor_inc18.io.Out(0)

  const6.io.enable <> bb_pfor_inc18.io.Out(1)

  binaryOp_inc3219.io.enable <> bb_pfor_inc18.io.Out(2)


  icmp_exitcond7220.io.enable <> bb_pfor_inc18.io.Out(3)


  br_21.io.enable <> bb_pfor_inc18.io.Out(4)


  const7.io.enable <> bb_sync_continue22.io.Out(0)

  const8.io.enable <> bb_sync_continue22.io.Out(1)

  binaryOp_add3423.io.enable <> bb_sync_continue22.io.Out(2)


  icmp_cmp224.io.enable <> bb_sync_continue22.io.Out(3)


  br_25.io.enable <> bb_sync_continue22.io.Out(4)


  //  <> bb_offload_loopk26.io.Out(1)
  call_27_in.io.enable.enq(ControlBundle.active())
  
  call_27_out.io.enable <> bb_offload_loopk26.io.Out(0)



  // add sync
  sync_28.io.enable <> bb_pfor_cond_cleanup28.io.Out(0)



  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phijj_0706.io.Mask <> bb_loopkk5.io.MaskBB(0)

  phikk_06913.io.Mask <> bb_loopi12.io.MaskBB(0)

  phi__begin_06816.io.Mask <> bb_pfor_detach15.io.MaskBB(0)



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phijj_0706.io.InData(0) <> const0.io.Out

  binaryOp_add379.io.RightIO <> const1.io.Out

  icmp_cmp10.io.RightIO <> const2.io.Out

  phikk_06913.io.InData(0) <> const3.io.Out

  phi__begin_06816.io.InData(0) <> const4.io.Out

  binaryOp_inc3219.io.RightIO <> const5.io.Out

  icmp_exitcond7220.io.RightIO <> const6.io.Out

  binaryOp_add3423.io.RightIO <> const7.io.Out

  icmp_cmp224.io.RightIO <> const8.io.Out

  binaryOp_add379.io.LeftIO <> phijj_0706.io.Out(1)

  icmp_cmp10.io.LeftIO <> binaryOp_add379.io.Out(1)

  br_11.io.CmpIO <> icmp_cmp10.io.Out(0)

  binaryOp_add3423.io.LeftIO <> phikk_06913.io.Out(1)

  binaryOp_inc3219.io.LeftIO <> phi__begin_06816.io.Out(0)

  // call_27_out.io.inVals.elements("field0") <> phi__begin_06816.io.Out(1)
  // call_27_out.io.In.elements("field5") <> phi__begin_06816.io.Out(1) // loop cilk i 

  icmp_exitcond7220.io.LeftIO <> binaryOp_inc3219.io.Out(1)

  br_21.io.CmpIO <> icmp_exitcond7220.io.Out(0)

  icmp_cmp224.io.LeftIO <> binaryOp_add3423.io.Out(1)

  br_25.io.CmpIO <> icmp_cmp224.io.Out(0)

  reattach_19.io.predicateIn(0).enq(DataBundle.active(1.U))



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  // call_0_out_io <> call_0_out.io.Out(0)

  // call_0_in.io.In <> call_0_in_io

  // br_2.io.PredOp(0) <> call_0_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  io.call_27_out_io <> call_27_out.io.Out(0)


  // for Ptrs
  // call_27_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field1")(0)
  // call_27_out.io.In.elements("field1") <> Loop_0.io.OutLiveIn.elements("field2")(0)
  // call_27_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field4")(0)

  // for Vals
  // call_27_out.io.In.elements("field3") <> Loop_0.io.OutLiveIn.elements("field3")(0) // loop jj
  // call_27_out.io.In.elements("field4") <> Loop_0.io.OutLiveIn.elements("field0")(0) // loop kk
  // call_27_out.io.In.elements("field5") <> phi__begin_06816.io.Out(1) // loop cilk i

  // io.call_4_out_io <> call_4_out.io.Out(0)

/*
  //Ptrs  //checked
  io.call_27_out_io.bits.dataPtrs.elements("field0") <> call_27_out.io.Out(0).bits.data.elements("field0") //dataPtrs(0) = m1
  io.call_27_out_io.bits.dataPtrs.elements("field1") <> call_27_out.io.Out(0).bits.data.elements("field1") //dataPtrs(1) = m2
  io.call_27_out_io.bits.dataPtrs.elements("field2") <> call_27_out.io.Out(0).bits.data.elements("field2") //dataPtrs(2) = prod

  //Val //checked
  io.call_27_out_io.bits.dataVals.elements("field0") <> call_27_out.io.Out(0).bits.data.elements("field5") // loop i
  io.call_27_out_io.bits.dataVals.elements("field1") <> call_27_out.io.Out(0).bits.data.elements("field4") // loop kk
  io.call_27_out_io.bits.dataVals.elements("field2") <> call_27_out.io.Out(0).bits.data.elements("field3") // loop jj

  // bug fixing
  call_27_out.io.Out(0).ready <> io.call_27_out_io.ready
  io.call_27_out_io.valid <> call_27_out.io.Out(0).valid
  io.call_27_out_io.bits.enable <> call_27_out.io.Out(0).bits.enable
*/
  call_27_in.io.In <> io.call_27_in_io

  reattach_19.io.enable <> call_27_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_4.io.Out

}

