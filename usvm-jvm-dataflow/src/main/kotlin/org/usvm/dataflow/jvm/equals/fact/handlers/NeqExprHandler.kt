package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.JcExpr
import org.jacodb.api.jvm.cfg.JcLocalVar
import org.jacodb.api.jvm.cfg.JcNeqExpr
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate.*
import org.usvm.dataflow.jvm.equals.fact.utils.negotiate
import org.usvm.dataflow.jvm.equals.fact.utils.toFact

class NeqExprHandler : InstructionHandler {
    override fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact {
        val neqExpr = expr as JcNeqExpr
        val lhv = neqExpr.lhv
        val rhv = neqExpr.rhv

        val lhvFact = if (lhv is JcLocalVar) {
            ctx.locationToFact.getFact(lhv.name, pathConstraintsInCurrentPoint) ?: Fact.Top
        } else {
            lhv.toFact()
        }
        val rhvFact = if (rhv is JcLocalVar) {
            ctx.locationToFact.getFact(rhv.name, pathConstraintsInCurrentPoint) ?: Predicate.Top
        } else {
            rhv.toFact()
        }
        // TODO: support connectives such && can also be used.

        // TODO: there can be reassignment with casts.
        if (rhvFact is True) {
            return (lhvFact as Predicate).negotiate()
        } else if (rhvFact is False) {
            return lhvFact
        } else if (lhvFact is ThisOrOther.This && rhvFact is ThisOrOther.Other ||
            lhvFact is ThisOrOther.Other && rhvFact is ThisOrOther.This
        ) {
            return NotEquals.NotEqualsObj
        } else if (lhvFact is ThisOrOther.This && rhvFact is Null ||
            lhvFact is Null && rhvFact is ThisOrOther.This
        ) {
            return IsNotNull(ThisOrOther.This)
        } else if (lhvFact is ThisOrOther.Other && rhvFact is Null ||
            lhvFact is Null && rhvFact is ThisOrOther.Other
        ) {
            return IsNotNull(ThisOrOther.Other)
        } else if (lhvFact is Cls && rhvFact is Cls &&
            (lhvFact.instance is ThisOrOther.This && rhvFact.instance is ThisOrOther.Other ||
                    lhvFact.instance is ThisOrOther.Other && rhvFact.instance is ThisOrOther.This)
        ) {
            return Equals.EqualsCls
        } else if (lhvFact is Field && rhvFact is Field && (
                    lhvFact.instance is ThisOrOther.This && rhvFact.instance is ThisOrOther.Other ||
                            lhvFact.instance is ThisOrOther.Other && rhvFact.instance is ThisOrOther.This
                    )
        ) {
            return Equals.EqualsField(lhvFact.name)
        }

        return Predicate.Top
    }
}