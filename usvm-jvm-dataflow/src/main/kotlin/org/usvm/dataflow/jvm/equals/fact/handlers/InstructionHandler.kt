package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcExpr
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact

interface InstructionHandler {
    // TODO: path constraints in current point - name it better.
    fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact
}