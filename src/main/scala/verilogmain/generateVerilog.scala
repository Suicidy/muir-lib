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
import dandelion.posit._
import dandelion.fpu._

object accelerator_edited_Main extends App {
  val dir = new File("RTL/accelerator_edited_Main/bgemm/float_16/child_4");
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
  val dir = new File("RTL/accelerator_edited_Main/bgemm/posit_32/child_4");
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


object posit_add extends App {
  val dir = new File("RTL/posit/add/16_1");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  // val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new PositALU(16,1,"fadd")))
  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

object float_add extends App {
  val dir = new File("RTL/float/add/16");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  // val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new FPUALU(16, "fadd", FType.H)))
  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}


object posit_mul extends App {
  val dir = new File("RTL/posit/mul/20_2");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  // val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new PositALU(20,2,"fmul")))
  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

object float_mul extends App {
  val dir = new File("RTL/float/mul/32");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  // val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new FPUALU(32, "fmul", FType.S)))
  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

object simpleReg extends App {
  val dir = new File("RTL/simpleReg");
  dir.mkdirs
  implicit val p = new WithAccelConfig ++ new WithTestConfig
  // val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Accelerator_edited(3,2,new Core_edited_posit(3,2))))
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new SimpleReg(3, 2)))
  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}