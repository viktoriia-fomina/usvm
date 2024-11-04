package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcArgument
import org.jacodb.api.jvm.cfg.JcCastExpr
import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcLocalVar
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.ThisOrOther
import org.usvm.dataflow.jvm.equals.fact.Fact.Top

class CastExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact {
        val castExpr = expr as JcCastExpr
        val operand = castExpr.operand

        // TODO: what if non local var, but concrete value? I should process it.
        if (operand is JcLocalVar) {
            // TODO: log case when local var is null.
            return ctx.locationToFact.getFact(operand.name, pathConstraintsInCurrentPoint) ?: Top
        }

        if (operand !is JcArgument) {
            return Top
        }

        if (castExpr.typeName == ctx.thisTypeName) {
            return ThisOrOther.Other
        }

        return Top
    }
}