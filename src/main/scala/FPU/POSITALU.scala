package dandelion.posit

import hardfloat._
import chisel3.Module
import chisel3._
import chisel3.util._
import posit._


object PositAluOpCode {
  val Add = 1
  val Sub = 2
  val Mul = 3
  val Mac = 4

  val opMap = Map(
    "Add" -> Add,
    "fadd" -> Add,
    "add" -> Add,
    "Sub" -> Sub,
    "sub" -> Sub,
    "fsub" -> Sub,
    "Mul" -> Mul,
    "mul" -> Mul,
    "fmul" -> Mul,
    "Mac" -> Mac,
    "mac" -> Mac
  )
}


class PositALU(val size: Int, val es: Int, val opCode: String) extends Module {
  val io = IO(new Bundle {
    val in1 = Input(Bits(size.W))
    val in2 = Input(Bits(size.W))
    val out = Output(Bits(size.W))
  })
    PositAluOpCode.opMap(opCode) match {
      case PositAluOpCode.Add => { // b + c
        val PositAdder = Module (new PositAdderSubtractor(size))
        val PositDecode_1 = Module(new DecodePosit(size))
        val PositDecode_2 = Module(new DecodePosit(size))
        val PositEncode = Module(new EncodePosit(size))
        PositDecode_1.io.i_bits := io.in1
        PositDecode_2.io.i_bits := io.in2

        PositDecode_1.io.i_es := es.U
        PositDecode_2.io.i_es := es.U

        PositAdder.io.i_posit_1 := PositDecode_1.io.o_posit
        PositAdder.io.i_posit_2 := PositDecode_2.io.o_posit
        PositAdder.io.i_op := 0.U

        PositEncode.io.i_posit := PositAdder.io.o_posit
        io.out := PositEncode.io.o_bits
      }
      case PositAluOpCode.Sub => { // b - c
        val PositSubtractor = Module (new PositAdderSubtractor(size))
        val PositDecode_1 = Module(new DecodePosit(size))
        val PositDecode_2 = Module(new DecodePosit(size))
        val PositEncode = Module(new EncodePosit(size))
        PositDecode_1.io.i_bits := io.in1
        PositDecode_2.io.i_bits := io.in2

        PositDecode_1.io.i_es := es.U
        PositDecode_2.io.i_es := es.U

        PositSubtractor.io.i_posit_1 := PositDecode_1.io.o_posit
        PositSubtractor.io.i_posit_2 := PositDecode_2.io.o_posit
        PositSubtractor.io.i_op := 1.U

        PositEncode.io.i_posit := PositSubtractor.io.o_posit
        io.out := PositEncode.io.o_bits
      }
      case PositAluOpCode.Mul => { // a*b
        val PositMul = Module (new PositMultiplier(size))
        val PositDecode_1 = Module(new DecodePosit(size))
        val PositDecode_2 = Module(new DecodePosit(size))
        val PositEncode = Module(new EncodePosit(size))
        PositDecode_1.io.i_bits := io.in1
        PositDecode_2.io.i_bits := io.in2

        PositDecode_1.io.i_es := es.U
        PositDecode_2.io.i_es := es.U

        PositMul.io.i_posit_1 := PositDecode_1.io.o_posit
        PositMul.io.i_posit_2 := PositDecode_2.io.o_posit

        PositEncode.io.i_posit := PositMul.io.o_posit
        io.out := PositEncode.io.o_bits
      }
    //   case PositAluOpCode.Mac => { // a*b + c
    //     mulAddRecFN.io.op := 0.U
    //     mulAddRecFN.io.a := in1RecFN
    //     mulAddRecFN.io.b := in2RecFN
    //     mulAddRecFN.io.c := dummy0.io.out
    //   }
    }

}