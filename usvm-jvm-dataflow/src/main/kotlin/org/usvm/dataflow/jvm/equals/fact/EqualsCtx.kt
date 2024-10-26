package org.usvm.dataflow.jvm.equals.fact

// TODO: get rid of mutability.
data class EqualsCtx(val thisTypeName: String, val locationToFact: MutableMap<String, Fact>)