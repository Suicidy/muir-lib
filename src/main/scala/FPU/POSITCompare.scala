package dandelion.posit

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import dandelion.config._
import chipsalliance.rocketchip.config._
// import muxes._
import dandelion.interfaces._
import util._
import dandelion.node._
import posit.{PositE, PositLE, PositL}

object PositCmpOpCode {
  val Less = 1
  val LessE = 2
  val Great = 3
  val GreatE = 4
  val Equal = 5
  val NEqual = 6

  val opMap = Map(
    "oeq" -> Equal,
    "one" -> NEqual,
    "ogt" -> Great,
    "oge" -> GreatE,
    "olt" -> Less,
    "ole" -> LessE
  )
}

/**
 * [FPComputeNodeIO description]
 */
class PositCompareNodeIO(NumOuts: Int)
                   (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new DataBundle()))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new DataBundle()))
}

/**
 * [FPCompareNode description]
 */
class PositCompareNode(NumOuts: Int, ID: Int, opCode: String)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle())(p) {
  override lazy val io = IO(new PositCompareNodeIO(NumOuts))

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate// & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/
  val mantisa = 3
  val posit_bit = 32


  PositCmpOpCode.opMap(opCode) match {
    case PositCmpOpCode.Less => {
      val PositLess = Module(new PositL(mantisa, posit_bit))
      PositLess.io.i_bits_1 := left_R.data
      PositLess.io.i_bits_2 := right_R.data
      out_data_R.data := PositLess.io.o_result
    }
    case PositCmpOpCode.LessE => {
      val PositLessE = Module(new PositLE(mantisa, posit_bit))
      PositLessE.io.i_bits_1 := left_R.data
      PositLessE.io.i_bits_2 := right_R.data
      out_data_R.data := PositLessE.io.o_result
    }
    case PositCmpOpCode.Great => {
      val PositLessE = Module(new PositLE(mantisa, posit_bit))
      PositLessE.io.i_bits_1 := left_R.data
      PositLessE.io.i_bits_2 := right_R.data
      out_data_R.data := !PositLessE.io.o_result
    }
    case PositCmpOpCode.GreatE => {
      val PositLess = Module(new PositL(mantisa, posit_bit))
      PositLess.io.i_bits_1 := left_R.data
      PositLess.io.i_bits_2 := right_R.data
      out_data_R.data := !PositLess.io.o_result
    }
    case PositCmpOpCode.Equal => {
      val PositEqual = Module(new PositE(mantisa, posit_bit))
      PositEqual.io.i_bits_1 := left_R.data
      PositEqual.io.i_bits_2 := right_R.data
      out_data_R.data := PositEqual.io.o_result
    }
    case PositCmpOpCode.NEqual => {
      val PositEqual = Module(new PositE(mantisa, posit_bit))
      PositEqual.io.i_bits_1 := left_R.data
      PositEqual.io.i_bits_2 := right_R.data
      out_data_R.data := !PositEqual.io.o_result
    }
  }

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire()) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire()) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut()
          when(enable_R.control) {
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset()
        printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] " + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, io.Out(0).bits.data)
      }
    }
  }

}
