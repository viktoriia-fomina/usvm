package org.usvm.dataflow.jvm.equals.fact.utils

import org.jacodb.api.jvm.cfg.JcArgument
import org.jacodb.api.jvm.cfg.JcNullConstant
import org.jacodb.api.jvm.cfg.JcThis
import org.jacodb.api.jvm.cfg.JcValue
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate.*

/**
 * When negotiation has no sense, e.g., for `Fact.Field`, then `Fact.Top` is returned.
 */
fun Fact.negotiate(): Fact = when (this) {
    is AndBinary -> OrBinary(this.fact1.negotiate(), this.fact2.negotiate())
    is OrBinary -> AndBinary(this.fact1.negotiate(), this.fact2.negotiate())
    is And -> Or(facts.map { it.negotiate() })
    is Or -> And(facts.map { it.negotiate() })
    is IsNull -> IsNotNull(this.instance)
    is IsNotNull -> IsNull(this.instance)
    is Equals.EqualsObj -> NotEquals.NotEqualsObj
    is NotEquals.NotEqualsObj -> Equals.EqualsObj
    is Equals.EqualsCls -> NotEquals.NotEqualsCls
    is Equals.EqualsField -> NotEquals.NotEqualsField(this.name)
    is NotEquals.NotEqualsField -> Equals.EqualsField(this.name)
    is True -> False
    is False -> True
    else -> Top
}

// TODO: in some cases we can process, we still return Fact.Top.
/**
 * When there's no obvious fact it cannot be cast to then `Fact.Top` is returned.
 *
 * Note! This all is used in context of `equals` method, e.g., `JcArgument` is converted to `Fact.ThisOrOther.Other`.
 */
fun JcValue.toFact() = when (this) {
    is JcNullConstant -> Null
    is JcThis -> ThisOrOther.This
    is JcArgument -> ThisOrOther.Other
    // TODO: process True/False facts.
    else -> Top
}

fun Fact.isStructural(): Boolean {
    return when (this) {
        is OrBinary -> this.fact1.isStructural() && this.fact2.isStructural()
        is AndBinary -> this.fact1.isStructural() && this.fact2.isStructural()
        is And -> this.facts.all { it.isStructural() }
        is Or -> this.facts.all { it.isStructural() }
        is Equals.EqualsField -> true
        Equals.EqualsCls -> true
        Equals.EqualsObj -> true
        // TODO: not sure about this NotEquals below.
        is IsNull -> true
        is IsNotNull -> true
        is NotEquals.NotEqualsField -> true
        is NotEquals.NotEqualsCls -> true
        is NotEquals.NotEqualsObj -> true
        else -> false
    }
}