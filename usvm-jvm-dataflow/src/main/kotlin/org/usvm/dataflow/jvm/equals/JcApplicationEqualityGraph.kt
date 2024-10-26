package org.usvm.dataflow.jvm.equals

import org.jacodb.api.jvm.JcClasspath
import org.jacodb.api.jvm.JcMethod
import org.jacodb.api.jvm.analysis.JcApplicationGraph
import org.jacodb.api.jvm.cfg.JcInst
import org.jacodb.api.jvm.ext.cfg.callExpr

class JcApplicationEqualityGraph(override val cp: JcClasspath) : JcApplicationGraph {
    override fun predecessors(node: JcInst): Sequence<JcInst> {
        val graph = node.location.method.flowGraph()
        val predecessors = graph.predecessors(node)
        val throwers = graph.throwers(node)
        return predecessors.asSequence() + throwers.asSequence()
    }

    override fun successors(node: JcInst): Sequence<JcInst> {
        val graph = node.location.method.flowGraph()
        val successors = graph.successors(node)
        val catchers = graph.catchers(node)
        return successors.asSequence() + catchers.asSequence()
    }

    override fun callees(node: JcInst): Sequence<JcMethod> {
        val callExpr = node.callExpr ?: return emptySequence()
        return sequenceOf(callExpr.method.method)
    }

    override fun callers(method: JcMethod): Sequence<JcInst> {
        TODO("Not yet implemented")
    }

    override fun entryPoints(method: JcMethod): Sequence<JcInst> {
        return method.flowGraph().entries.asSequence()
    }

    override fun exitPoints(method: JcMethod): Sequence<JcInst> {
        return method.flowGraph().exits.asSequence()
    }

    override fun methodOf(node: JcInst): JcMethod {
        return node.location.method
    }
}