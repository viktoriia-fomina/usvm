package org.usvm.dataflow.jvm.equals

import org.jacodb.api.jvm.JcClasspath
import org.jacodb.api.jvm.JcMethod
import org.jacodb.api.jvm.analysis.JcApplicationGraph
import org.jacodb.api.jvm.cfg.*
import org.usvm.dataflow.jvm.equals.fact.CollectFactsResult
import org.usvm.dataflow.jvm.equals.fact.EqualsCtx
import org.usvm.dataflow.jvm.equals.fact.Fact
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.handlers.*
import org.usvm.dataflow.jvm.equals.fact.utils.isStructural
import org.usvm.dataflow.jvm.equals.fact.utils.negotiate
import org.usvm.dataflow.jvm.util.thisInstance

class EqualsProcessor {
    // TODO:
    //   1. Refactor EqualsProcessor.
    //   2. Process cases when trying to use location that has not been calculated so far.
    //   3. Refactor EqExprHandler/NeqExprHandler.
    //   4. Process cases when different values can be stored to the same location, but in different paths.
    //   5. Use persistent map for path constraints.
    //   6. Add logging.
    companion object {
        /**
         * @return true when considers `equals` as structural;
         * false even when it is not sure whether `equals` structural or not.
         */
        fun isEqualsStructural(cp: JcClasspath, equalsMethod: JcMethod): Boolean {
            val equalsMethodInsts = equalsMethod.instList
            val (locationToFact, returnToPathConstraints) = collectFacts(cp, equalsMethod)
            println(locationToFact)

            val returnInsts = equalsMethodInsts.filterIsInstance<JcReturnInst>()
            val res = returnInsts.all { returnInst ->
                val returnValue = returnInst.returnValue
                val pathConstraints = returnToPathConstraints[returnInst]
                val pathConstraintFact = pathConstraints?.let { And(it) }

                when (returnValue) {
                    is JcLocalVar -> {
                        val fact = locationToFact[returnValue.name]
                        // TODO: log it, error case when fact == null.
                        fact != null && fact != Top && fact.isStructural() && pathConstraintFact?.isStructural() != false
                    }
                    null -> pathConstraintFact?.isStructural() != false
                    else -> {
                        when(returnValue) {
                            is JcInt -> when (returnValue.value) {
                                    0 -> pathConstraintFact?.negotiate()?.isStructural() != false
                                    1 -> pathConstraintFact?.isStructural() != false
                                    else -> false
                                }

                            else -> false
                        }
                    }
                }
            }

            return res
        }

        private fun collectFacts(cp: JcClasspath, method: JcMethod): CollectFactsResult {
            val appGraph = JcApplicationEqualityGraph(cp)
            return collectFactsWithDfs(appGraph, method)
        }

        private fun collectFactsWithDfs(
            appGraph: JcApplicationGraph,
            method: JcMethod
        ): CollectFactsResult {
            val firstInst = method.instList.first()
            val ctx = EqualsCtx(method.thisInstance.typeName, mutableMapOf())

            val stack = mutableListOf(firstInst)
            val visited = mutableSetOf<JcInst>()
            // TODO: delete sub-branches we have already analyzed.
            val pathConstraints = mutableMapOf<JcInst, List<Fact>>()

            while (stack.isNotEmpty()) {
                val vertex = stack.removeLast()

                if (vertex !in visited) {
                    visited.add(vertex)
                    processVertex(appGraph, ctx, vertex, stack, pathConstraints)
                }
            }

            // TODO: we should contain path constraints only for JcReturnInst s.
            return CollectFactsResult(ctx.locationToFact, pathConstraints)
        }

        private fun processVertex(
            appGraph: JcApplicationGraph,
            ctx: EqualsCtx,
            vertex: JcInst,
            stack: MutableList<JcInst>,
            pathConstraints: MutableMap<JcInst, List<Fact>>,
        ) {
            if (vertex is JcAssignInst) {
                handleAssignInst(vertex, ctx)
            }

            val successors = appGraph.successors(vertex)
            if (vertex is JcIfInst) {
                handleIfInst(appGraph, ctx, vertex, pathConstraints)
            } else {
                val vertexConstraints = pathConstraints[vertex]
                    ?.toMutableList()
                    ?: mutableListOf()
                successors.forEach { pathConstraints[it] = vertexConstraints }
            }

            stack.addAll(successors)
        }

        private fun handleIfInst(
            appGraph: JcApplicationGraph,
            ctx: EqualsCtx,
            vertex: JcIfInst,
            pathConstraints: MutableMap<JcInst, List<Fact>>
        ) {
            val method = appGraph.methodOf(vertex)
            val successors = appGraph.successors(vertex)
            val vertexConstraints = pathConstraints[vertex]
                ?.toMutableList()
                ?: mutableListOf()
            val newConstraint = getIfCondition(vertex, ctx)
            // TODO: do we always have two of them?
            // TODO: I need all instructions list here to extract instruction.
            successors
                .find { it == method.instList[vertex.trueBranch.index] }
                ?.let { pathConstraints[it] = vertexConstraints + newConstraint }

            successors
                .find { it == method.instList[vertex.falseBranch.index] }
                ?.let { pathConstraints[it] = vertexConstraints + newConstraint.negotiate() }
        }

        private fun handleAssignInst(
            vertex: JcAssignInst,
            ctx: EqualsCtx
        ) {
            if (vertex.lhv is JcLocalVar) {
                val localVarName = (vertex.lhv as JcLocalVar).name
                val handler = handlers[vertex.rhv::class.java]
                ctx.locationToFact[localVarName] = handler?.handle(vertex.rhv, ctx) ?: Top
            }
        }

        private fun getIfCondition(
            vertex: JcIfInst,
            ctx: EqualsCtx
        ): Fact = when (val cond = vertex.condition) {
            is JcEqExpr, is JcNeqExpr -> {
                val handler = handlers[cond::class.java]
                handler?.handle(cond, ctx) ?: Top
            }

            else -> Top
        }

        private val handlers = mapOf(
            JcFieldRef::class.java to FieldRefHandler(),
            JcCastExpr::class.java to CastExprHandler(),
            JcStaticCallExpr::class.java to StaticCallExprHandler(),
            JcVirtualCallExpr::class.java to VirtualCallExprHandler(),
            JcInstanceOfExpr::class.java to InstanceOfExprHandler(),
            JcEqExpr::class.java to EqExprHandler(),
            JcNeqExpr::class.java to NeqExprHandler(),
            JcInt::class.java to BooleanHandler()
        )
    }
}
