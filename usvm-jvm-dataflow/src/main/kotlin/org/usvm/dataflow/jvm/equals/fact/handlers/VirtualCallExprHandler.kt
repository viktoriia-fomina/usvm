package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcArgument
import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcThis
import org.jacodb.api.jvm.cfg.JcVirtualCallExpr
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*

class VirtualCallExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact {
        val virtualCallExpr = expr as JcVirtualCallExpr
        if (virtualCallExpr.args.isNotEmpty() ||
            virtualCallExpr.method.name != "getClass" ||
            virtualCallExpr.typeName != "java.lang.Class<*>"

        ) {
            return Top
        }

        return when (virtualCallExpr.instance) {
            is JcThis -> Cls(ThisOrOther.This)
            is JcArgument -> Cls(ThisOrOther.Other)
            else -> Top
        }
    }
}