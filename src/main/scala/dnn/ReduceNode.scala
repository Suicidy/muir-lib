package dnn

import chisel3._
import chisel3.util.{Decoupled, Enum, Valid}
import chisel3.{Bundle, Flipped, Module, Output, RegInit, UInt, assert, printf, when}
import config.{Parameters, XLEN}
import config._
import dnn.types.{OperatorReduction}
import interfaces.CustomDataBundle
//import javafx.scene.chart.PieChart.Data
import node.{AluGenerator, HandShakingIONPS, HandShakingNPS, Shapes}

class ReduceFU[L <: Shapes : OperatorReduction](left: => L, pipelined: Boolean, opCode: String)
                                               (implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(left))
    val o = Output(Valid(UInt(p(XLEN).W)))
  })


  val start = io.a.valid
  val FU    = OperatorReduction.magic(io.a.bits, io.a.valid, pipelined, opCode)
  io.o.bits := FU._1
  val latency = FU._2
  val latCnt  = Module(new SatCounterModule(latency))
  latCnt.io.start := start
  io.o.valid := latCnt.io.wrap
}

class ReduceIO[L <: Shapes](NumOuts: Int)(left: => L)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt(p(XLEN).W))) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new CustomDataBundle(UInt((left.getWidth).W))))

  override def cloneType = new ReduceIO(NumOuts)(left).asInstanceOf[this.type]
}

class ReduceNode[L <: Shapes : OperatorReduction](NumOuts: Int, ID: Int, pipelined: Boolean, opCode: String)(left: => L)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new CustomDataBundle(UInt(p(XLEN).W)))(p) {
  override lazy val io = IO(new ReduceIO(NumOuts)(left))

  /*===========================================*
 *            Registers                      *
 *===========================================*/
  // OP Inputs
  val left_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))

  // Output register
  val data_R = RegInit(CustomDataBundle.default(0.U((xlen).W)))

  val s_idle :: s_LATCH :: s_ACTIVE :: s_COMPUTE :: Nil = Enum(4)
  val state                                             = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = left_R.predicate & IsEnable( )
  val start     = left_R.valid & IsEnableValid( )

  /*===============================================*
   *            Latch inputs. Wire up left       *
   *===============================================*/

  // Predicate register
  val pred_R = RegInit(init = false.B)

  //printfInfo("start: %x\n", start)

  io.LeftIO.ready := ~left_R.valid
  when(io.LeftIO.fire( )) {
    //printfInfo("Latch left data\n")
    left_R.data := io.LeftIO.bits.data
    left_R.valid := true.B
    left_R.predicate := io.LeftIO.bits.predicate
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_R.data
    io.Out(i).bits.valid := true.B
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := left_R.taskID | enable_R.taskID
  }

  /*============================================*
 *            ACTIONS (possibly dangerous)    *
 *============================================*/

  val FU = Module(new ReduceFU(left, pipelined, opCode))
  FU.io.a.bits := (left_R.data).asTypeOf(left)

  data_R.predicate := predicate
  pred_R := predicate
  FU.io.a.valid := false.B
  //  This is written like this to enable FUs that are dangerous in the future.
  // If you don't start up then no value passed into function
  when(start & state === s_idle) {
    when(predicate) {
      FU.io.a.valid := true.B
      state := s_ACTIVE
    }.otherwise {
      state := s_COMPUTE
      ValidOut( )
    }
  }

  when(state === s_ACTIVE) {
    when(FU.io.o.valid) {
      ValidOut( )
      data_R.data := (FU.io.o.bits).asTypeOf(UInt(left.getWidth.W))
      data_R.valid := FU.io.o.valid
      state := s_COMPUTE
    }.otherwise {
      state := s_ACTIVE
    }
  }
  when(IsOutReady( ) && state === s_COMPUTE) {
    left_R := CustomDataBundle.default(0.U((left.getWidth).W))
    data_R := CustomDataBundle.default(0.U((left.getWidth).W))
    Reset( )
    state := s_idle
  }


  /**
    * Cant print value with more than 64bits.
    * In this example value is > 64bits
    */
  //printf(p"\n State : ${state} Output: ${data_R}")

  var classname: String = (left.getClass).toString
  var signed            = "S"
  override val printfSigil =
    opCode + "[" + classname.replaceAll("class node.", "") + "]_" + ID + ":"

  if (log == true && (comp contains "TYPOP")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {
      }
      case "med" => {
      }
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"Left\": %x},", x, (left_R.valid))
        printf("\"State\": {\"State\": \"%x\", \"(L)\": \"%x,%x\",  \"O(V,D,P)\": \"%x,%x,%x\" },", state, left_R.data, io.Out(0).valid, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {
      }
    }
  }
}


