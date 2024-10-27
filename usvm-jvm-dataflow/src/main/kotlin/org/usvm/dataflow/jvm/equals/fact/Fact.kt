package org.usvm.dataflow.jvm.equals.fact

sealed class Fact {
    data object Top : Fact()

    sealed class ThisOrOther : Fact() {
        data object This : ThisOrOther()

        data object Other : ThisOrOther()
    }

    data object Null : Fact()

    data object True : Fact()

    data object False : Fact()

    data class Field(val instance: ThisOrOther, val name: String) : Fact()

    /**
     * `Cls(This)` corresponds to `this.getClass()`.
     */
    data class Cls(val instance: ThisOrOther) : Fact()

    sealed class Predicate : Fact() {
        data class IsNull(val instance: ThisOrOther) : Predicate()

        data class IsNotNull(val instance: ThisOrOther) : Predicate()

        data object IsThisCls : Predicate()

        data object IsNotThisClass : Predicate()

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

    data class AndBinary(val fact1: Fact, val fact2: Fact) : Fact()

    // TODO: check list size > 1.
    data class And(val facts: List<Fact>) : Fact()

    data class OrBinary(val fact1: Fact, val fact2: Fact) : Fact()

    data class Or(val facts: List<Fact>) : Fact()
}
