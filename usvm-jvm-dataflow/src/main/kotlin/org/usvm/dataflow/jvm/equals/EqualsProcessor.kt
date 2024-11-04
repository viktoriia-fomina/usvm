package org.usvm.dataflow.jvm.equals

import org.jacodb.api.jvm.JcClasspath
import org.jacodb.api.jvm.JcMethod
import org.jacodb.api.jvm.analysis.JcApplicationGraph
import org.jacodb.api.jvm.cfg.*
import org.usvm.dataflow.jvm.equals.fact.*
import org.usvm.dataflow.jvm.equals.fact.Fact.*
import org.usvm.dataflow.jvm.equals.fact.handlers.*
import org.usvm.dataflow.jvm.equals.fact.utils.isStructural
import org.usvm.dataflow.jvm.equals.fact.utils.negotiate
import org.usvm.dataflow.jvm.util.thisInstance

class EqualsProcessor {
    // TODO:
    //   1. Refactor EqualsProcessor.
    //   2. Process cases when trying to use location that has not been calculated so far (is it possible???).
    //   3. Refactor EqExprHandler/NeqExprHandler.
    //   4. Use persistent map for path constraints.
    //   5. Add logging.
    //   6. Fix case when to the same location can be added different values within a single function:
    //   if-else block, { } - any different scope (?).
    companion object {
        /**
         * @return true when considers `equals` as structural;
         * false even when it is not sure whether `equals` structural or not.
         */
        fun isEqualsStructural(cp: JcClasspath, equalsMethod: JcMethod): Boolean {
            val equalsMethodInsts = equalsMethod.instList
            val (locationToFact, returnToPathConstraints) = collectFacts(cp, equalsMethod) // TODO: process empty map
            println(locationToFact)

            val returnInsts = equalsMethodInsts.filterIsInstance<JcReturnInst>()
            val res = returnInsts.all { returnInst ->
                val returnValue = returnInst.returnValue
                val pathConstraints = returnToPathConstraints.get(returnInst)

                when (returnValue) {
                    is JcLocalVar -> {
                        val fact = locationToFact.getFact(returnValue.name, pathConstraints)
                        // TODO: log it, error case when fact == null.
                        fact != null && fact != Top && fact.isStructural() && pathConstraints?.isStructural() != false
                    }

                    null -> pathConstraints?.isStructural() != false
                    else -> {
                        when (returnValue) {
                            is JcInt -> when (returnValue.value) {
                                0 -> pathConstraints?.negotiate()?.isStructural() != false
                                1 -> pathConstraints?.isStructural() != false
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
            val firstInst = method.instList.firstOrNull()
                ?: return CollectFactsResult(LocationToFact(), PathConstraints())
            val ctx = EqualsCtx(method.thisInstance.typeName, LocationToFact())

            val stack = ArrayDeque<JcInst>().apply { add(firstInst) }
            val visited = mutableSetOf<JcInst>()
            // TODO: delete sub-branches we have already analyzed.
            val pathConstraints = PathConstraints()

            while (stack.isNotEmpty()) {
                val vertex = stack.removeLast()

                if (vertex !in visited) {
                    visited.add(vertex)
                    processVertex(appGraph, ctx, vertex, stack, visited, pathConstraints)
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
            visited: MutableSet<JcInst>,
            pathConstraints: PathConstraints,
        ) {
            val vertexConstraints = pathConstraints.get(vertex)

            if (vertex is JcAssignInst) {
                processAssignInst(vertex, vertexConstraints, ctx)
            }

            val successors = appGraph.successors(vertex)
            if (vertex is JcIfInst) {
                processIfInst(appGraph, ctx, vertex, visited, pathConstraints)
            } else {
                successors.forEach { succ ->
                    vertexConstraints?.let { pathConstraints.add(succ, it) }
                }
            }

            stack.addAll(successors)
        }

        private fun processIfInst(
            appGraph: JcApplicationGraph,
            ctx: EqualsCtx,
            vertex: JcIfInst,
            visited: MutableSet<JcInst>,
            pathConstraints: PathConstraints
        ) {
            val method = appGraph.methodOf(vertex)
            val successors = appGraph.successors(vertex)
            val vertexConstraints = pathConstraints.get(vertex)
            val newConstraint = getIfCondition(vertex, ctx, vertexConstraints)
            // TODO: do we always have two of them?
            val trueBranchSucc = successors.find { it == method.instList[vertex.trueBranch.index] }
            trueBranchSucc?.let {
                pathConstraints.add(it, vertexConstraints and newConstraint)
            }

            val falseBranchSucc = successors.find { it == method.instList[vertex.falseBranch.index] }
            falseBranchSucc?.let {
                pathConstraints.add(it, vertexConstraints and newConstraint.negotiate())
            }

            // If the path constraints of a successor vertex change,
            // add it back to the worklist to ensure its successors are updated correctly
            // (also for successors of a successor etc).
            visited.remove(trueBranchSucc)
            visited.remove(falseBranchSucc)
        }

        private fun processAssignInst(
            vertex: JcAssignInst,
            vertexConstraints: Fact?,
            ctx: EqualsCtx
        ) {
            if (vertex.lhv is JcLocalVar) {
                val localVarName = (vertex.lhv as JcLocalVar).name
                val handler = handlers[vertex.rhv::class.java]
                val vertexFact = handler?.handle(vertex.rhv, ctx, vertexConstraints) ?: Top
                ctx.locationToFact.add(localVarName, vertexFact, vertexConstraints)
            }
        }

        private fun getIfCondition(
            vertex: JcIfInst,
            ctx: EqualsCtx,
            vertexConstraints: Fact?
        ): Predicate = when (val cond = vertex.condition) {
            is JcEqExpr, is JcNeqExpr -> {
                val handler = handlers[cond::class.java]
                // TODO: log case when non predicate is returned!!!
                handler?.handle(cond, ctx, vertexConstraints) as? Predicate ?: Predicate.Top
            }

            else -> Predicate.Top
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
