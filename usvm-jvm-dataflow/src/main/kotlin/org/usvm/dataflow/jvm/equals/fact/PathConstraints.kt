package org.usvm.dataflow.jvm.equals.fact

import org.jacodb.api.jvm.cfg.JcInst
import org.usvm.dataflow.jvm.equals.fact.Fact.Predicate

// TODO: not every fact can be a constraint.
class PathConstraints {
    private val pathConstraints = mutableMapOf<JcInst, Predicate>()

    fun add(inst: JcInst, instConstraints: Predicate) {
        val currentInstConstraints = pathConstraints[inst]

        if (currentInstConstraints == null) {
            pathConstraints[inst] = instConstraints
        } else {
            pathConstraints[inst] = currentInstConstraints or instConstraints
        }
    }

    fun get(inst: JcInst) = pathConstraints[inst]
}