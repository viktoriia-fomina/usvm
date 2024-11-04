package org.usvm.dataflow.jvm.equals.fact

data class ConstrainedFact(val fact: Fact, val pathConstraints: Fact?)