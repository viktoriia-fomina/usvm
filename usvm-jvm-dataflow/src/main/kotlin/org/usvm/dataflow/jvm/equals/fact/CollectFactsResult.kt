package org.usvm.dataflow.jvm.equals.fact

import org.jacodb.api.jvm.cfg.JcInst

data class CollectFactsResult(
    val locationToFact: Map<String, Fact>,
    val returnToPathConstraints: Map<JcInst, List<Fact>>
)
