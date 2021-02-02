package dandelion.posit

import chisel3._
import chisel3.util._
import dandelion.interfaces._
import chipsalliance.rocketchip.config._
import dandelion.config._
import util._
import posit._


class PositComputeNodeIO(NumOuts: Int)
                   (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new DataBundle()))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new DataBundle()))

  override def cloneType = new PositComputeNodeIO(NumOuts).asInstanceOf[this.type]

}


/**
 * [PositComputeNode description]
 */
class PositComputeNode(NumOuts: Int, ID: Int, opCode: String)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle())(p) {
  override lazy val io = IO(new PositComputeNodeIO(NumOuts))


  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "

  val (cycleCount, _) = Counter(true.B, 32 * 1024)

   /*===========================================*
   *            Function Unit                      *
   *===========================================*/
  val posit_bit = xlen
  val es = if (xlen == 32) 3 else 2;


  val PositU = Module (new PositALU(posit_bit, es, opCode))

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  //Output register
  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)


  val out_data_R = RegNext(Mux(enable_R.control, PositU.io.out, 0.U), init = 0.U)


  val predicate = Mux(enable_valid_R, enable_R.control ,io.enable.bits.control)

  val taskID = Mux(enable_valid_R, enable_R.taskID ,io.enable.bits.taskID)


  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  PositU.io.in1 := left_R.data
  PositU.io.in2 := right_R.data
  
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


  io.Out.foreach(_.bits := DataBundle(out_data_R, taskID, predicate))
  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut()
          io.Out.foreach(_.bits := DataBundle(PositU.io.out, taskID, predicate))
          io.Out.map(_.valid) foreach(_ := true.B)
          left_valid_R := false.B
          right_valid_R := false.B
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset data
        out_data_R := 0.U
        //Reset state
        state := s_IDLE
        Reset()
      }
    }
  }

}