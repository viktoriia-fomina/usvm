package org.usvm.dataflow.jvm.equals.fact.utils

import org.jacodb.api.jvm.cfg.*
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate.*
import org.usvm.dataflow.jvm.equals.fact.and
import org.usvm.dataflow.jvm.equals.fact.or

fun Predicate.negotiate(): Predicate = when (this) {
    is Predicate.Top -> Predicate.Top
    is And -> predicates.reduce { acc, f -> acc or f.negotiate() }
    is Or -> predicates.reduce { acc, f -> acc and f.negotiate() }
    is IsNull -> IsNotNull(this.instance)
    is IsNotNull -> IsNull(this.instance)
    is IsThisCls -> IsNotThisClass
    is IsNotThisClass -> IsThisCls
    is Equals.EqualsObj -> NotEquals.NotEqualsObj
    is NotEquals.NotEqualsObj -> Equals.EqualsObj
    is Equals.EqualsCls -> NotEquals.NotEqualsCls
    is NotEquals.NotEqualsCls -> Equals.EqualsCls
    is Equals.EqualsField -> NotEquals.NotEqualsField(this.name)
    is NotEquals.NotEqualsField -> Equals.EqualsField(this.name)
    is True -> False
    is False -> True
}

// TODO: in some cases we can process, we still return Fact.Top.
/**
 * When there's no obvious fact it cannot be cast to then `Fact.Top` is returned.
 *
 * Note! This all is used in context of `equals` method, e.g., `JcArgument` is converted to `Fact.ThisOrOther.Other`.
 */
fun JcValue.toFact() = when (this) {
    is JcThis -> ThisOrOther.This
    is JcArgument -> ThisOrOther.Other
    is JcNullConstant -> Null
    is JcBool -> if (this.value) True else False
    else -> Fact.Top
}

fun Fact.isStructural(): Boolean {
    return when (this) {
        is And -> this.predicates.all { it.isStructural() }
        is Or -> this.predicates.all { it.isStructural() }
        is Equals.EqualsField -> true
        Equals.EqualsCls -> true
        Equals.EqualsObj -> true
        // TODO: not sure about this NotEquals below.
        is IsNull -> true
        is IsNotNull -> true
        is IsThisCls -> true
        is IsNotThisClass -> true
        is NotEquals.NotEqualsField -> true
        is NotEquals.NotEqualsCls -> true
        is NotEquals.NotEqualsObj -> true
        else -> false
    }
}

fun Fact.toPredicate(): Predicate? = when (this) {
    is Predicate -> this
    is Fact.Top -> Predicate.Top
    else -> null
}