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
        val PositAdder = Module (new PositAdd(es, size))
        PositAdder.io.i_bits_1 := io.in1
        PositAdder.io.i_bits_2 := io.in2
        io.out := PositAdder.io.o_bits
      }
      case PositAluOpCode.Sub => { // b - c
        val PositSubtractor = Module (new PositSub(es, size))
        PositSubtractor.io.i_bits_1 := io.in1
        PositSubtractor.io.i_bits_2 := io.in2
        io.out := PositSubtractor.io.o_bits 
      }
      case PositAluOpCode.Mul => { // a*b
        val PositMultiplier = Module (new PositMul(es, size))
        PositMultiplier.io.i_bits_1 := io.in1
        PositMultiplier.io.i_bits_2 := io.in2
        io.out := PositMultiplier.io.o_bits
      }
    //   case PositAluOpCode.Mac => { // a*b + c
    //     mulAddRecFN.io.op := 0.U
    //     mulAddRecFN.io.a := in1RecFN
    //     mulAddRecFN.io.b := in2RecFN
    //     mulAddRecFN.io.c := dummy0.io.out
    //   }
    }

}