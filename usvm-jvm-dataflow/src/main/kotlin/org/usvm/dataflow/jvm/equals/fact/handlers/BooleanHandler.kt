package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcInt
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*

class BooleanHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx): Fact {
        val boolean = expr as JcInt
        return when (boolean.value) {
            0 -> False
            1 -> True
            else -> Top
        }
    }
}