package org.usvm.dataflow.jvm.equals.fact

data class CollectFactsResult(
    val locationToFact: LocationToFact,
    val returnToPathConstraints: PathConstraints
)
