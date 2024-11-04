package org.usvm.dataflow.jvm.equals.fact

import org.usvm.dataflow.jvm.equals.fact.Fact.*

sealed class Fact {
    data object Top : Fact()

    sealed class ThisOrOther : Fact() {
        data object This : ThisOrOther()

        data object Other : ThisOrOther()
    }

    data object Null : Fact()

    data class Field(val instance: ThisOrOther, val name: String) : Fact()

    /**
     * `Cls(This)` corresponds to `this.getClass()`.
     */
    data class Cls(val instance: ThisOrOther) : Fact()

    sealed class Predicate : Fact() {
        // TODO: think that Top can be misused with another Top.
        data object Top : Predicate()

        data object True : Predicate()

        data object False : Predicate()

        data class IsNull(val instance: ThisOrOther) : Predicate()

        data class IsNotNull(val instance: ThisOrOther) : Predicate()

        data object IsThisCls : Predicate()

        data object IsNotThisClass : Predicate()

        // TODO: check list size > 1.
        data class And(val predicates: List<Predicate>) : Predicate()

        data class Or(val predicates: List<Predicate>) : Predicate()

        sealed class Equals : Predicate() {
            /**
             * this == other
             */
            data object EqualsObj : Equals()

            /**
             * this and other field with name [name] are equal
             */
            data class EqualsField(val name: String) : Equals()

            /**
             * this and other are of the same class
             */
            data object EqualsCls : Equals()
        }

        sealed class NotEquals : Predicate() {
            /**
             * this != other
             */
            data object NotEqualsObj : NotEquals()

            /**
             * this and other field with name [name] are not equal
             */
            data class NotEqualsField(val name: String) : NotEquals()

            /**
             * this and other are not of the same class
             */
            data object NotEqualsCls : NotEquals()
        }
    }
}

// TODO: make connectives structure better.

// TODO: not any fact can be connected with connectives.
infix fun Predicate?.or(p: Predicate): Predicate = when {
    this == null -> p
    this is Predicate.Or -> Predicate.Or(this.predicates + listOf(p))
    p is Predicate.Or -> Predicate.Or(listOf(this) + p.predicates)
    else -> Predicate.Or(listOf(this, p))
}

infix fun Predicate?.and(p: Predicate): Predicate = when {
    this == null -> p
    this is Predicate.And -> Predicate.And(this.predicates + listOf(p))
    p is Predicate.And -> Predicate.And(listOf(this) + p.predicates)
    else -> Predicate.And(listOf(this, p))
}

// TODO: create some function like simplify, e.g., something like TOP or TOP can be simplified to TOP.