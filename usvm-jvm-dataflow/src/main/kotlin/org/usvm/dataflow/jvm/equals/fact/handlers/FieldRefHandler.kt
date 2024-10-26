package org.usvm.dataflow.jvm.equals.fact.handlers

import org.jacodb.api.jvm.cfg.*
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*

class FieldRefHandler : InstructionHandler {
    // TODO: check somewhere whether correct one expr is passed.
    override fun handle(expr: JcExpr, ctx: EqualsCtx): Fact {
        val fieldRef = expr as JcFieldRef
        val field = fieldRef.field

        // Can start from %, e.g., %3.name.
        val instanceFact = when (val instance = fieldRef.instance) {
            is JcThis -> ThisOrOther.This
            is JcArgument -> ThisOrOther.Other
            // TODO: does JcLocal var always mean there is sth starting from %?
            is JcLocalVar -> ctx.locationToFact[instance.name] ?: Top
            else -> Top
        }
        // TODO: don't do such not really safe casting.
        return if (instanceFact == Top) Top else Field(instanceFact as ThisOrOther, field.name)
    }
}