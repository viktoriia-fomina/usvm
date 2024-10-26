package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcLocalVar
import org.jacodb.api.jvm.cfg.JcStaticCallExpr
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.Field
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate.Equals
import org.usvm.dataflow.jvm.equals.fact.Fact.Top

class StaticCallExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx): Fact {
        val staticCallExpr = expr as JcStaticCallExpr
        val method = staticCallExpr.method

        // TODO: remove redundant checks?
        if (staticCallExpr.typeName == "boolean" && staticCallExpr.args.size == 2 &&
            (method.enclosingType.typeName == "java.util.Objects" && method.name == "equals" ||
                    method.enclosingType.typeName == "kotlin.jvm.internal.Intrinsics" && method.name == "areEqual")
        ) {
            // TODO: can arg not to be JcLocalVar?
            val arg0 = ctx.locationToFact[(staticCallExpr.args[0] as JcLocalVar).name] ?: Top
            val arg1 = ctx.locationToFact[(staticCallExpr.args[1] as JcLocalVar).name] ?: Top

            // TODO: can be arg0 not a Field?
            if (arg0 is Field && arg1 is Field && arg0.name == arg1.name) {
                return Equals.EqualsField(arg0.name)
            }
        }

        return Top
    }
}