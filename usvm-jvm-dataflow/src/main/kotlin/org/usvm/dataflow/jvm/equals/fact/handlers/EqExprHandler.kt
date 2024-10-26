package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcEqExpr
import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcLocalVar
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate.Equals.*
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate.IsNull
import org.usvm.dataflow.jvm.equals.fact.utils.toFact


// TODO: mostly copy-pasted from NeqExprHandler so get rid of copy pasting.
class EqExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx): Fact {
        val eqExpr = expr as JcEqExpr
        val lhv = eqExpr.lhv
        val rhv = eqExpr.rhv

        // TODO: it does not look beautiful.
        val lhvFact = if (lhv is JcLocalVar) {
            ctx.locationToFact[lhv.name] ?: Top
        } else {
            lhv.toFact()
        }
        val rhvFact = if (rhv is JcLocalVar) {
            ctx.locationToFact[rhv.name] ?: Top
        } else {
            rhv.toFact()
        }

        // TODO: there can be reassignment with casts etc.
        if (lhvFact is ThisOrOther.This && rhvFact is ThisOrOther.Other ||
            lhvFact is ThisOrOther.Other && rhvFact is ThisOrOther.This
        ) {
            return EqualsObj
        } else if (lhvFact is ThisOrOther.This && rhvFact is Null ||
            lhvFact is Null && rhvFact is ThisOrOther.This
        ) {
            return IsNull(ThisOrOther.This)
        } else if (lhvFact is ThisOrOther.Other && rhvFact is Null ||
            lhvFact is Null && rhvFact is ThisOrOther.Other
        ) {
            return IsNull(ThisOrOther.Other)
        } else if (lhvFact is Cls && rhvFact is Cls &&
            (lhvFact.instance is ThisOrOther.This && rhvFact.instance is ThisOrOther.Other ||
                    lhvFact.instance is ThisOrOther.Other && rhvFact.instance is ThisOrOther.This)
        ) {
            return EqualsCls
        } else if (lhvFact is Field && rhvFact is Field && (
                    lhvFact.instance is ThisOrOther.This && rhvFact.instance is ThisOrOther.Other ||
                            lhvFact.instance is ThisOrOther.Other && rhvFact.instance is ThisOrOther.This
                    )
        ) {
            return EqualsField(lhvFact.name)
        }

        return Top
    }
}