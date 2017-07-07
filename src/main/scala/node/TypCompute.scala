package node

import chisel3._
import chisel3.iotesters.{ ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester }
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{ Matchers, FlatSpec }

import config._
import interfaces._
import muxes._
import util._

class Numbers(implicit p: Parameters) extends CoreBundle()(p) {
}

class vec2(implicit p: Parameters) extends Numbers {
  val data = Vec(2, UInt(16.W))
}

class mat2x2(implicit p: Parameters) extends Numbers {
  val data = Vec(2, Vec(2, UInt(16.W)))
}

object operation {

  trait OperatorLike[T] {
    def addition(l: T, r: T)(implicit p: Parameters): T
    def subtraction(l: T, r: T)(implicit p: Parameters): T
  }

  object OperatorLike {
    implicit object mat2x2likeNumber extends OperatorLike[mat2x2] {
      def addition(l: mat2x2, r: mat2x2)(implicit p: Parameters): mat2x2 = {
        val x = Wire(new mat2x2)
        for (i <- 0 until 2) {
          for (j <- 0 until 2) {
            x.data(i)(j) := l.data(i)(j) + r.data(i)(j)
          }
        }
        x
      }
      def subtraction(l: mat2x2, r: mat2x2)(implicit p: Parameters): mat2x2 = {
        val x = Wire(new mat2x2)
        for (i <- 0 until 2) {
          for (j <- 0 until 2) {
            x.data(i)(j) := l.data(i)(j) - r.data(i)(j)
          }
        }
        x
      }
    }

    implicit object vec2likeNumber extends OperatorLike[vec2] {
      def addition(l: vec2, r: vec2)(implicit p: Parameters): vec2 = {
        val x = Wire(new vec2)
        x.data(0)(0) := l.data(0)(0) + r.data(0)(0)
        x.data(0)(1) := l.data(0)(1) + r.data(0)(1)
        x
      }

      def subtraction(l: vec2, r: vec2)(implicit p: Parameters): vec2 = {
        val x = Wire(new vec2)
        x.data(0)(0) := l.data(0)(0) - r.data(0)(0)
        x.data(0)(1) := l.data(0)(1) - r.data(0)(1)
        x
      }

    }
  }
    def addition[T](l: T, r: T)(implicit op: OperatorLike[T], p: Parameters): T = op.addition(l, r)
    def subtraction[T](l: T, r: T)(implicit op: OperatorLike[T], p: Parameters): T = op.subtraction(l, r)
  
}

import operation._

class OperatorModule[T <: Numbers: OperatorLike](gen: => T)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(gen))
    val b = Flipped(Valid(gen))
    val o = Output(Valid(gen))
  })

  io.o.valid := io.a.valid && io.b.valid
  io.o.bits := addition(io.a.bits, io.b.bits)
  printf(p"Left: ${io.a.bits} Right: ${io.b.bits} Output: ${io.o.bits}")
}

class TypComputeIO(NumOuts: Int)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new TypBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new TypBundle))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new TypBundle))
}

class TypCompute[T <: Numbers: OperatorLike](NumOuts: Int, ID: Int, opCode: String)(sign: Boolean)(gen: => T)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new TypBundle)(p) {
  override lazy val io = IO(new TypComputeIO(NumOuts))
  // Printf debugging
  override val printfSigil = "Compute[T] ID_" + ID + ":"

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // OP Inputs
  val left_R = RegInit(TypBundle.default)

  // Memory Response
  val right_R = RegInit(TypBundle.default)

  // Output register
  val data_R = RegInit(TypBundle.default)

  val s_idle :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = left_R.predicate & right_R.predicate & IsEnable()
  val start = left_R.valid & right_R.valid & IsEnableValid()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  // Predicate register
  val pred_R = RegInit(init = false.B)

  //printfInfo("start: %x\n", start)

  io.LeftIO.ready := ~left_R.valid
  when(io.LeftIO.fire()) {
    //printfInfo("Latch left data\n")
    state := s_LATCH
    left_R.data := io.LeftIO.bits.data
    left_R.valid := true.B
    left_R.predicate := io.LeftIO.bits.predicate
  }

  io.RightIO.ready := ~right_R.valid
  when(io.RightIO.fire()) {
    //printfInfo("Latch right data\n")
    state := s_LATCH
    right_R.data := io.RightIO.bits.data
    right_R.valid := true.B
    right_R.predicate := io.RightIO.bits.predicate
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_R.data
    io.Out(i).bits.predicate := predicate
  }

  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  val FU = Module(new OperatorModule(gen))

  FU.io.a.bits := (left_R.data).asTypeOf(gen)
  FU.io.b.bits := (right_R.data).asTypeOf(gen)
  data_R.data := (FU.io.o.bits).asTypeOf(UInt(Typ_SZ.W))
  pred_R := predicate

  //  This is written like this to enable FUs that are dangerous in the future.
  // If you don't start up then no value passed into function
  when(start & predicate) {
    state := s_COMPUTE
    FU.io.a.valid := left_R.valid
    FU.io.b.valid := right_R.valid
    data_R.valid := FU.io.o.valid
    // Next cycle it will become valid.
    ValidOut()
  }.elsewhen(start & ~predicate) {
    ValidOut()
  }

  when(IsOutValid() && IsOutReady()) {
    left_R := TypBundle.default
    right_R := TypBundle.default
    data_R := TypBundle.default
    Reset()
    state := s_idle
  }

  printfInfo(" state: %x data_R : %x data_R.valid: %x \n", state, data_R.data, data_R.valid)
}