module OperatorModule(
  input   clock,
  input   reset
);
  wire  _T_3 = ~reset; // @[TypCompute.scala 87:15]
  always @(posedge clock) begin
    `ifndef SYNTHESIS
    `ifdef PRINTF_COND
      if (`PRINTF_COND) begin
    `endif
        if (_T_3) begin
          $fwrite(32'h80000002,"Left: Vec(Vec(%d, %d), Vec(%d, %d))\n",32'h0,32'h0,32'h0,32'h0); // @[TypCompute.scala 87:15]
        end
    `ifdef PRINTF_COND
      end
    `endif
    `endif // SYNTHESIS
  end
endmodule
module TypCompute(
  input   clock,
  input   reset
);
  wire  FU_clock; // @[TypCompute.scala 322:18]
  wire  FU_reset; // @[TypCompute.scala 322:18]
  OperatorModule FU ( // @[TypCompute.scala 322:18]
    .clock(FU_clock),
    .reset(FU_reset)
  );
  assign FU_clock = clock;
  assign FU_reset = reset;
endmodule
module TypeMemDataFlow(
  input         clock,
  input         reset,
  input  [31:0] io_dummy
);
  wire  typadd_clock; // @[TypeMemDataFlow.scala 106:22]
  wire  typadd_reset; // @[TypeMemDataFlow.scala 106:22]
  TypCompute typadd ( // @[TypeMemDataFlow.scala 106:22]
    .clock(typadd_clock),
    .reset(typadd_reset)
  );
  assign typadd_clock = clock;
  assign typadd_reset = reset;
endmodule
