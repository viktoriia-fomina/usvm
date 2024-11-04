package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcInstanceOfExpr
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate
import org.usvm.dataflow.jvm.equals.fact.Fact.Top

class InstanceOfExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact {
        val instanceOfExpr = expr as JcInstanceOfExpr

        return if (instanceOfExpr.targetType.typeName == ctx.thisTypeName) {
            Predicate.IsThisCls
        } else {
            Top
        }
    }
}