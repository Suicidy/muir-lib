// See LICENSE for license details.

package dandelion.node

import chisel3._
import chisel3.util._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import dandelion.config._





// Tester.
class GepTester(df: GepOneNode)
                  (implicit p: Parameters) extends PeekPokeTester(df)  {

  poke(df.io.idx1.bits.data, 2.U)
  poke(df.io.idx1.valid, false.B)

  poke(df.io.baseAddress.bits.data, 1.U)
  poke(df.io.baseAddress.valid, false.B)

  poke(df.io.enable.bits.control , false.B)
  poke(df.io.enable.valid, false.B)

  poke(df.io.Out(0).ready, false.B)
  println(s"Output: ${peek(df.io.Out(0))}\n")


  step(1)

  poke(df.io.idx1.valid, true.B)
  poke(df.io.baseAddress.valid, true.B)

  poke(df.io.enable.bits.control , true.B)
  poke(df.io.enable.valid, true.B)
  poke(df.io.Out(0).ready, true.B)


  println(s"Output: ${peek(df.io.Out(0))}\n")

  println(s"t: -1\n -------------------------------------")
  step(1)


  for( i <- 0 until 10){
    println(s"Output: ${peek(df.io.Out(0))}\n")

    println(s"t: ${i}\n -------------------------------------")
    step(1)
  }

  poke(df.io.baseAddress.bits.data, 7.U)
  poke(df.io.baseAddress.valid, false.B)

  poke(df.io.idx1.bits.data, 8.U)
  poke(df.io.idx1.valid, false.B)

  step(1)
  poke(df.io.baseAddress.valid, true.B)
  poke(df.io.idx1.valid, true.B)


  for( i <- 0 until 10){
    println(s"Output: ${peek(df.io.Out(0))}\n")

    println(s"t: ${i}\n -------------------------------------")
    step(1)
  }

  val new_param = p.alterPartial(
    {case TRACE => true}
  )
  if(df.isDebug()){

   // println(s"STATE of GepNode is : 0x${peek(df.io.LogCheck.get.bits.data.asUInt())}\n")
  }


}


class GepTests extends  FlatSpec with Matchers {
   implicit val p = Parameters.root((new MiniConfig).toInstance)
  it should "Dataflow tester" in {
     chisel3.iotesters.Driver(() => new GepOneNode(NumOuts = 1, ID = 0, Debug = true)(numByte1 = 2)) {
       c => new GepTester(c)
     } should be(true)
   }
 }



