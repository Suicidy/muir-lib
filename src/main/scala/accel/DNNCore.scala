package accel

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import arbiters.TypeStackFile
import chisel3.{when, _}
import chisel3.util._
import config._
import control.BasicBlockNoMaskNode
import dnn.memory.{ReadTensorController, TensorLoad, TensorMaster, TensorStore, WriteTensorController, inDMA_act, inDMA_wgt, outDMA_act}
import interfaces.{ControlBundle, CustomDataBundle, DataBundle}
import junctions.SplitCallNew
import node.{FXmatNxN, UnTypStore, matNxN, vecN}
import shell._
import dnn.memory.ISA._
import dnn_layers.{DW_Block, DW_PW_Block}
import dnnnode.{Mac2dTensor, ShapeTransformer, StoreQueue, TLoad, TStore, WeightShapeTransformer}
import firrtl.transforms.DontTouchAnnotation

/** Core.
  *
  * The DNNcore defines the current DNN accelerator by connecting memory and
  * compute modules together such as load/store and compute. Most of the
  * connections in the core are bulk (<>), and we should try to keep it this
  * way, because it is easier to understand what is going on.
  *
  * Also, the DNNcore must be instantiated by a shell using the
  * VTA Control Register (VCR) and the VTA Memory Engine (VME) interfaces.
  * More info about these interfaces and modules can be found in the shell
  * directory.
  */
class DNNCore(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val vcr = new VCRClient
    val vme = new VMEMaster
  })

  val cycle_count = new Counter(2000)

  val memShape = new vecN(16, 0, false)
  val macDWShape = new matNxN(3, false)
  val macPWShape = new vecN(2, 0, false)

  val wgtDWShape = new vecN(9, 0, false)
  val wgtPWShape = new vecN(1, 0, false)

  val NumChannel = 2
  val MACperCH = 2
  val NumPWFilter = 1

//  val DW_B1 = Module(new DW_Block(3, "wgt", "inp")(memShape)(wgtDWShape)(macDWShape))

  val conv = Module(new DW_PW_Block(NumChannel, MACperCH, NumPWFilter, "wgt", "wgtPW", "inp")
                   (memShape)(wgtDWShape)(wgtPWShape)(macDWShape)(macPWShape))

  /* ================================================================== *
     *                      Basic Block signals                         *
     * ================================================================== */
  conv.io.wgtDWIndex := 0.U
  conv.io.wgtPWIndex := 0.U

  conv.io.wgtPW_baddr := 1.U

  conv.io.outRowWidth := 18.U

  /* ================================================================== *
     *                           Connections                            *
     * ================================================================== */

  io.vcr.ecnt(0).bits := cycle_count.value

  for (i <- 0 until NumChannel * (MACperCH + macDWShape.getLength() - 1)) {
    io.vme.rd(i) <> conv.io.vme_rd(i)
  }

  for (i <- 0 until NumPWFilter * MACperCH) {
    io.vme.wr(i) <> conv.io.vme_wr(i)
  }

  io.vme.rd(8) <> conv.io.vme_wgtDW_rd
  io.vme.rd(9) <> conv.io.vme_wgtPW_rd

  conv.io.start := false.B

  conv.io.inBaseAddr := io.vcr.ptrs(0)
  conv.io.wgtDW_baddr := io.vcr.ptrs(1)
  conv.io.wgtPW_baddr := io.vcr.ptrs(1)

  conv.io.outBaseAddr := io.vcr.ptrs(2)

  val sIdle :: sExec :: sFinish :: Nil = Enum(3)

  val state = RegInit(sIdle)
  switch(state) {
    is(sIdle) {
      when(io.vcr.launch) {
        conv.io.start := true.B
        state := sExec
      }
    }
    is(sExec) {
      when(conv.io.done) {
        state := sIdle
      }
    }
  }

  val last = state === sExec && conv.io.done
  io.vcr.finish := last
  io.vcr.ecnt(0).valid := last

  when(state =/= sIdle) {
    cycle_count.inc()
  }
}
