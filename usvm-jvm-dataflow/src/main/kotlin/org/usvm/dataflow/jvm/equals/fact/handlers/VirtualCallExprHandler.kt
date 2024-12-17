package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.*
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.isTop

class VirtualCallExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact {
        val virtualCallExpr = expr as JcVirtualCallExpr
        // TODO: extract checking       pluon equals logic (it also exists in StaticCallExprHandler).
        // TODO: check somehow this equals is overridden.
        if (virtualCallExpr.args.size == 1 && virtualCallExpr.method.name == "equals" &&
            virtualCallExpr.method.parameters.first().type.typeName == "java.lang.Object"
        ) {
            val instance = when (virtualCallExpr.instance) {
                // TODO: get rid of cast here.
                is JcLocalVar ->
                    ctx.locationToFact.getFact(
                        (virtualCallExpr.instance as JcLocalVar).name,
                        pathConstraintsInCurrentPoint
                    ) ?: Top
                is JcArgument -> ThisOrOther.Other
                is JcThis -> ThisOrOther.This

                else -> return Top
            }
            val arg = when (virtualCallExpr.args.first()) {
                is JcLocalVar ->
                    ctx.locationToFact.getFact(
                        (virtualCallExpr.args.first() as JcLocalVar).name,
                        pathConstraintsInCurrentPoint
                    ) ?: Top
                is JcArgument -> ThisOrOther.Other
                is JcThis -> ThisOrOther.This

                else -> return Top
            }

            if (instance.isTop() || arg.isTop()) {
                return Top
                // TODO: get rid of this copy-pasted logic.
            } else if (instance is Field && arg is Field && instance.name == arg.name &&
                (instance.instance is ThisOrOther.This && arg.instance is ThisOrOther.Other ||
                        instance.instance is ThisOrOther.Other && arg.instance is ThisOrOther.This)
            ) {
                return Predicate.Equals.EqualsField(instance.name)
            } else if (instance is ThisOrOther.This && arg is ThisOrOther.Other || // TODO: is it possible?
                instance is ThisOrOther.Other && arg is ThisOrOther.This) {
                return Predicate.Equals.EqualsObj
            } else {
                Top
            }
        }

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