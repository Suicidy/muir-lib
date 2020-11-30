package verilogmain

//            liveIn_R(i).predicate := io.latchEnable.bits.control
//liveIn_R(i).predicate := io.latchEnable.bits.control
import java.io.{File, FileWriter}

import dandelion.node._
import chipsalliance.rocketchip.config._
import dandelion.config._
import dandelion.interfaces._
import dandelion.arbiters._
import dandelion.memory._
import dandelion.dataflow._
import chipsalliance.rocketchip.config._
import util._
import dandelion.generator._
import dandelion.interfaces._
import dandelion.accel._
import posit._

object accelerator_edited_Main extends App {
  val dir = new File("RTL/accelerator_edited_Main/bgemm/float_32/child_4");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited(3,2))))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

object accelerator_edited_Main_posit extends App {
  val dir = new File("RTL/accelerator_edited_Main/bgemm/posit_16/child_3");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}


// object posit_div extends App {
//   val dir = new File("RTL/posit/div");
//   dir.mkdirs
//   // implicit val p = new WithAccelConfig ++ new WithTestConfig
//   // val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))
//   val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new PositDiv(3,32)))
//   val verilogFile = new File(dir, s"${chirrtl.main}.v")
//   val verilogWriter = new FileWriter(verilogFile)
//   val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
//   val compiledStuff = compileResult.getEmittedCircuit
//   verilogWriter.write(compiledStuff.value)
//   verilogWriter.close()
// }
