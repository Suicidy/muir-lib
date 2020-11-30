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
import utility.Constants._
import dandelion.memory.stack._
import dandelion.node._
import muxes._
import org.scalatest._
import regfile._
import util._


class bgemm_detach1DF(PtrsIn: Seq[Int] = List(32, 32, 32), ValsIn: Seq[Int] = List(32, 32, 32), Returns: Seq[Int] = List())
			(implicit p: Parameters) extends DandelionAccelDCRModule(PtrsIn, ValsIn, Returns){


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Cache
  // val mem_ctrl_cache = Module(new CacheMemoryEngine(ID = 0, NumRead = 3, NumWrite = 1))

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 3, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))


  // io.MemReq <> mem_ctrl_cache.io.cache.MemReq
  // mem_ctrl_cache.io.cache.MemResp <> io.MemResp

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val ArgSplitter = Module(new SplitCallDCR(ptrsArgTypes = List(1, 1, 1), valsArgTypes = List(1, 1, 1)))
  ArgSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 2, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(2, 2, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_loopk1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 1))

  val bb_my_for_body114 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 10, NumPhi = 1, BID = 4))

  val bb_my_for_body2013 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 18, NumPhi = 1, BID = 13))

  val bb_my_for_cond_cleanup1929 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 29))

  val bb_my_for_cond_cleanup933 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 33))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = shl nsw i32 %__begin.068.in, 2, !UID !4 == i_row
  val binaryOp_2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "shl")(sign = false, Debug = false))

  //  br label %my_for.body11, !UID !5, !BB_UID !6
  val br_3 = Module(new UBranchNode(ID = 3))

  //  %1 = phi i32 [ 0, %my_loopk ], [ %21, %my_for.cond.cleanup19 ], !UID !7
  val phi5 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 5, Res = true))

  //  %2 = add nuw nsw i32 %1, %kk.069.in, !UID !8
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "add")(sign = false, Debug = false))

  //  %3 = shl i32 %2, 2, !UID !9
  val binaryOp_7 = Module(new ComputeNode(NumOuts = 1, ID = 7, opCode = "shl")(sign = false, Debug = false))

  //  %4 = add nuw nsw i32 %1, %kk.069.in, !UID !10
  val binaryOp_8 = Module(new ComputeNode(NumOuts = 1, ID = 8, opCode = "add")(sign = false, Debug = false))

  //  %5 = add nuw nsw i32 %4, %0, !UID !11
  val binaryOp_9 = Module(new ComputeNode(NumOuts = 1, ID = 9, opCode = "add")(sign = false, Debug = false))

  //  %6 = getelementptr inbounds float, float* %m1.in, i32 %5, !UID !12
  val Gep_10 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 10)(ElementSize = 4, ArraySize = List()))

  //  %7 = load float, float* %6, align 4, !tbaa !13, !UID !17
  // val ld_11 = Module(new UnTypLoadCache(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 11, RouteID = 0))
  val ld_11 = Module(new UnTypLoad(Typ= MT_W, NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 11, RouteID = 0))

  //  br label %my_for.body20, !UID !18, !BB_UID !19
  val br_12 = Module(new UBranchNode(ID = 12))

  //  %8 = phi i32 [ 0, %my_for.body11 ], [ %19, %my_for.body20 ], !UID !20
  val phi14 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 14, Res = true))

  //  %9 = add nuw nsw i32 %8, %jj.070.in, !UID !21
  val binaryOp_15 = Module(new ComputeNode(NumOuts = 1, ID = 15, opCode = "add")(sign = false, Debug = false))

  //  %10 = add nuw nsw i32 %9, %3, !UID !22
  val binaryOp_16 = Module(new ComputeNode(NumOuts = 1, ID = 16, opCode = "add")(sign = false, Debug = false))

  //  %11 = getelementptr inbounds float, float* %m2.in, i32 %10, !UID !23
  val Gep_17 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 17)(ElementSize = 4, ArraySize = List()))

  //  %12 = load float, float* %11, align 4, !tbaa !13, !UID !24
  // val ld_18 = Module(new UnTypLoadCache(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 18, RouteID = 1))
  val ld_18 = Module(new UnTypLoad(Typ= MT_W, NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 18, RouteID = 1))

  //  %13 = fmul float %7, %12, !UID !25
  val FP_19 = Module(new FPComputeNode(NumOuts = 1, ID = 19, opCode = "fmul")(fType))

  //  %14 = add nuw nsw i32 %8, %jj.070.in, !UID !26
  val binaryOp_20 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "add")(sign = false, Debug = false))

  //  %15 = add nuw nsw i32 %14, %0, !UID !27
  val binaryOp_21 = Module(new ComputeNode(NumOuts = 1, ID = 21, opCode = "add")(sign = false, Debug = false))

  //  %16 = getelementptr inbounds float, float* %prod.in, i32 %15, !UID !28
  val Gep_22 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 22)(ElementSize = 4, ArraySize = List()))

  //  %17 = load float, float* %16, align 4, !tbaa !13, !UID !29
  // val ld_23 = Module(new UnTypLoadCache(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 23, RouteID = 2))
  val ld_23 = Module(new UnTypLoad(Typ= MT_W, NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 23, RouteID = 2))

  //  %18 = fadd float %17, %13, !UID !30
  val FP_24 = Module(new FPComputeNode(NumOuts = 1, ID = 24, opCode = "fadd")(fType))

  //  store float %18, float* %16, align 4, !tbaa !13, !UID !31
  // val st_25 = Module(new UnTypStoreCache(NumPredOps = 0, NumSuccOps = 1, ID = 25, RouteID = 3))
  val st_25 = Module(new UnTypStore(Typ= MT_W, NumPredOps = 0, NumSuccOps = 1, ID = 25, RouteID = 0))

  //  %19 = add nuw nsw i32 %8, 1, !UID !32
  val binaryOp_26 = Module(new ComputeNode(NumOuts = 2, ID = 26, opCode = "add")(sign = false, Debug = false))

  //  %20 = icmp eq i32 %19, 2, !UID !33
  val icmp_27 = Module(new ComputeNode(NumOuts = 1, ID = 27, opCode = "eq")(sign = false, Debug = false))

  //  br i1 %20, label %my_for.cond.cleanup19, label %my_for.body20, !UID !34, !BB_UID !35
  val br_28 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 1, ID = 28))

  //  %21 = add nuw nsw i32 %1, 1, !UID !36
  val binaryOp_30 = Module(new ComputeNode(NumOuts = 2, ID = 30, opCode = "add")(sign = false, Debug = false))

  //  %22 = icmp eq i32 %21, 2, !UID !37
  val icmp_31 = Module(new ComputeNode(NumOuts = 1, ID = 31, opCode = "eq")(sign = false, Debug = false))

  //  br i1 %22, label %my_for.cond.cleanup9, label %my_for.body11, !UID !38, !BB_UID !39
  val br_32 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 32))

  //  ret void, !UID !40, !BB_UID !41
  val ret_34 = Module(new RetNode2(retTypes = List(), ID = 34))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 2
  val const0 = Module(new ConstFastNode(value = 4, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 2
  val const2 = Module(new ConstFastNode(value = 4, ID = 2))

  //i32 0
  val const3 = Module(new ConstFastNode(value = 0, ID = 3))

  //i32 1
  val const4 = Module(new ConstFastNode(value = 1, ID = 4))

  //i32 2
  val const5 = Module(new ConstFastNode(value = 8, ID = 5))

  //i32 1
  val const6 = Module(new ConstFastNode(value = 1, ID = 6))

  //i32 2
  val const7 = Module(new ConstFastNode(value = 8, ID = 7))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_loopk1.io.predicateIn(0) <> ArgSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_my_for_body114.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_my_for_body114.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_my_for_body2013.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_my_for_body2013.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_my_for_cond_cleanup1929.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_my_for_cond_cleanup933.io.predicateIn(0) <> Loop_1.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_12.io.Out(0)

  Loop_0.io.loopBack(0) <> br_28.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_28.io.TrueOutput(0)

  Loop_1.io.enable <> br_3.io.Out(0)

  Loop_1.io.loopBack(0) <> br_32.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_32.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> binaryOp_7.io.Out(0)

  Loop_0.io.InLiveIn(1) <> ld_11.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field4")(0)

  Loop_0.io.InLiveIn(5) <> Loop_1.io.OutLiveIn.elements("field5")(0)

  Loop_1.io.InLiveIn(0) <> ArgSplitter.io.Out.dataVals.elements("field1")(0)

  Loop_1.io.InLiveIn(1) <> binaryOp_2.io.Out(0) //i_row 

  Loop_1.io.InLiveIn(2) <> ArgSplitter.io.Out.dataPtrs.elements("field0")(0)

  Loop_1.io.InLiveIn(3) <> ArgSplitter.io.Out.dataVals.elements("field2")(0)

  Loop_1.io.InLiveIn(4) <> ArgSplitter.io.Out.dataPtrs.elements("field1")(0)

  Loop_1.io.InLiveIn(5) <> ArgSplitter.io.Out.dataPtrs.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  binaryOp_16.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  FP_19.io.LeftIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  binaryOp_21.io.RightIO <> Loop_0.io.OutLiveIn.elements("field2")(0)

  binaryOp_15.io.RightIO <> Loop_0.io.OutLiveIn.elements("field3")(0)

  binaryOp_20.io.RightIO <> Loop_0.io.OutLiveIn.elements("field3")(1)

  Gep_17.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field4")(0)

  Gep_22.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field5")(0)

  binaryOp_6.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(0)

  binaryOp_8.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(1)

  binaryOp_9.io.RightIO <> Loop_1.io.OutLiveIn.elements("field1")(1)

  Gep_10.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_26.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_30.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi14.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi5.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_my_loopk1.io.Out(0)

  binaryOp_2.io.enable <> bb_my_loopk1.io.Out(1)


  br_3.io.enable <> bb_my_loopk1.io.Out(2)


  const1.io.enable <> bb_my_for_body114.io.Out(0)

  const2.io.enable <> bb_my_for_body114.io.Out(1)

  phi5.io.enable <> bb_my_for_body114.io.Out(2)


  binaryOp_6.io.enable <> bb_my_for_body114.io.Out(3)


  binaryOp_7.io.enable <> bb_my_for_body114.io.Out(4)


  binaryOp_8.io.enable <> bb_my_for_body114.io.Out(5)


  binaryOp_9.io.enable <> bb_my_for_body114.io.Out(6)


  Gep_10.io.enable <> bb_my_for_body114.io.Out(7)


  ld_11.io.enable <> bb_my_for_body114.io.Out(8)


  br_12.io.enable <> bb_my_for_body114.io.Out(9)


  const3.io.enable <> bb_my_for_body2013.io.Out(0)

  const4.io.enable <> bb_my_for_body2013.io.Out(1)

  const5.io.enable <> bb_my_for_body2013.io.Out(2)

  phi14.io.enable <> bb_my_for_body2013.io.Out(3)


  binaryOp_15.io.enable <> bb_my_for_body2013.io.Out(4)


  binaryOp_16.io.enable <> bb_my_for_body2013.io.Out(5)


  Gep_17.io.enable <> bb_my_for_body2013.io.Out(6)


  ld_18.io.enable <> bb_my_for_body2013.io.Out(7)


  FP_19.io.enable <> bb_my_for_body2013.io.Out(8)


  binaryOp_20.io.enable <> bb_my_for_body2013.io.Out(9)


  binaryOp_21.io.enable <> bb_my_for_body2013.io.Out(10)


  Gep_22.io.enable <> bb_my_for_body2013.io.Out(11)


  ld_23.io.enable <> bb_my_for_body2013.io.Out(12)


  FP_24.io.enable <> bb_my_for_body2013.io.Out(13)


  st_25.io.enable <> bb_my_for_body2013.io.Out(14)


  binaryOp_26.io.enable <> bb_my_for_body2013.io.Out(15)


  icmp_27.io.enable <> bb_my_for_body2013.io.Out(16)


  br_28.io.enable <> bb_my_for_body2013.io.Out(17)


  const6.io.enable <> bb_my_for_cond_cleanup1929.io.Out(0)

  const7.io.enable <> bb_my_for_cond_cleanup1929.io.Out(1)

  binaryOp_30.io.enable <> bb_my_for_cond_cleanup1929.io.Out(2)


  icmp_31.io.enable <> bb_my_for_cond_cleanup1929.io.Out(3)


  br_32.io.enable <> bb_my_for_cond_cleanup1929.io.Out(4)


  ret_34.io.In.enable <> bb_my_for_cond_cleanup933.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi5.io.Mask <> bb_my_for_body114.io.MaskBB(0)

  phi14.io.Mask <> bb_my_for_body2013.io.MaskBB(0)



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_11.io.memReq
  ld_11.io.memResp <> MemCtrl.io.ReadOut(0)
  MemCtrl.io.ReadIn(1) <> ld_18.io.memReq
  ld_18.io.memResp <> MemCtrl.io.ReadOut(1)
  MemCtrl.io.ReadIn(2) <> ld_23.io.memReq
  ld_23.io.memResp <> MemCtrl.io.ReadOut(2)
  MemCtrl.io.WriteIn(0) <> st_25.io.memReq
  st_25.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  binaryOp_2.io.RightIO <> const0.io.Out

  phi5.io.InData(0) <> const1.io.Out

  binaryOp_7.io.RightIO <> const2.io.Out

  phi14.io.InData(0) <> const3.io.Out

  binaryOp_26.io.RightIO <> const4.io.Out

  icmp_27.io.RightIO <> const5.io.Out

  binaryOp_30.io.RightIO <> const6.io.Out

  icmp_31.io.RightIO <> const7.io.Out

  binaryOp_6.io.LeftIO <> phi5.io.Out(0)

  binaryOp_8.io.LeftIO <> phi5.io.Out(1)

  binaryOp_30.io.LeftIO <> phi5.io.Out(2)

  binaryOp_7.io.LeftIO <> binaryOp_6.io.Out(0)

  binaryOp_9.io.LeftIO <> binaryOp_8.io.Out(0)

  Gep_10.io.idx(0) <> binaryOp_9.io.Out(0)

  ld_11.io.GepAddr <> Gep_10.io.Out(0)

  binaryOp_15.io.LeftIO <> phi14.io.Out(0)

  binaryOp_20.io.LeftIO <> phi14.io.Out(1)

  binaryOp_26.io.LeftIO <> phi14.io.Out(2)

  binaryOp_16.io.LeftIO <> binaryOp_15.io.Out(0)

  Gep_17.io.idx(0) <> binaryOp_16.io.Out(0)

  ld_18.io.GepAddr <> Gep_17.io.Out(0)

  FP_19.io.RightIO <> ld_18.io.Out(0)

  FP_24.io.RightIO <> FP_19.io.Out(0)

  binaryOp_21.io.LeftIO <> binaryOp_20.io.Out(0)

  Gep_22.io.idx(0) <> binaryOp_21.io.Out(0)

  ld_23.io.GepAddr <> Gep_22.io.Out(0)

  st_25.io.GepAddr <> Gep_22.io.Out(1)

  FP_24.io.LeftIO <> ld_23.io.Out(0)

  st_25.io.inData <> FP_24.io.Out(0)

  icmp_27.io.LeftIO <> binaryOp_26.io.Out(1)

  br_28.io.CmpIO <> icmp_27.io.Out(0)

  icmp_31.io.LeftIO <> binaryOp_30.io.Out(1)

  br_32.io.CmpIO <> icmp_31.io.Out(0)

  binaryOp_2.io.LeftIO <> ArgSplitter.io.Out.dataVals.elements("field0")(0)

  st_25.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  br_28.io.PredOp(0) <> st_25.io.SuccOp(0)



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_34.io.Out

}

