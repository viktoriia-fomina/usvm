package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcExpr
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact

interface InstructionHandler {
    fun handle(expr: JcExpr, ctx: EqualsCtx): Fact
}