package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.*
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*

class FieldRefHandler : InstructionHandler {
    // TODO: check somewhere whether correct one expr is passed.
    override fun handle(expr: JcExpr, ctx: EqualsCtx, pathConstraintsInCurrentPoint: Fact?): Fact {
        val fieldRef = expr as JcFieldRef
        val field = fieldRef.field

        // Can start from %, e.g., %3.name.
        val instanceFact = when (val instance = fieldRef.instance) {
            is JcThis -> ThisOrOther.This
            is JcArgument -> ThisOrOther.Other
            // TODO: does JcLocal var always mean there is sth starting from %?
            is JcLocalVar -> ctx.locationToFact.getFact(instance.name, pathConstraintsInCurrentPoint) ?: Top
            else -> Top
        }
        // TODO: don't do such not really safe casting.
        return when (instanceFact) {
            Top -> Top
            // TODO: why INSTANCE can be a field? Think about it
            is Field -> instanceFact
            else -> Field(instanceFact as ThisOrOther, field.name)
        }
    }
}