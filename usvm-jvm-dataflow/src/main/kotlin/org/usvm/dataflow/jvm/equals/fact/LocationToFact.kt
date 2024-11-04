package org.usvm.dataflow.jvm.equals.fact

import org.usvm.dataflow.jvm.equals.fact.Fact.*

// TODO: seems like it should be internal
class LocationToFact {
    private val locationToFact: MutableMap<String, MutableList<ConstrainedFact>> =
        mutableMapOf()

    fun add(location: String, fact: Fact, pathConstraints: Fact?) {
        // TODO: do not add already added element.

        val facts = locationToFact[location]

        if (facts == null) {
            locationToFact[location] = mutableListOf(ConstrainedFact(fact, pathConstraints))
        } else {
            locationToFact[location]?.add(ConstrainedFact(fact, pathConstraints))
        }
    }

    fun getFact(location: String, pathConstraintsInCurrentPoint: Fact?): Fact? {
        val locationInfo = locationToFact[location]
            ?: return null

        // TODO:
        //  1. There can be multiple usages of the location.
        //  2. I should know what location usage to choose for, e.g., return %1.

        // TODO: log somehow null case. Seems like it can be created something with scopes with {},
        //  but for now I consider it as an error case.
        return locationInfo.firstOrNull {
            isRightLocationInfo(it.pathConstraints, pathConstraintsInCurrentPoint)
        }?.fact // TODO: null case is an error, log it.
    }

    // TODO: rename method.
    private fun isRightLocationInfo(locationConstraints: Fact?, pathConstraintsInCurrentPoint: Fact?): Boolean = when {
        locationConstraints == null && pathConstraintsInCurrentPoint == null -> true
        locationConstraints == null && pathConstraintsInCurrentPoint != null -> true
        locationConstraints != null && pathConstraintsInCurrentPoint == null -> false
        locationConstraints == pathConstraintsInCurrentPoint -> true
        locationConstraints is Predicate.And -> {
            if (pathConstraintsInCurrentPoint !is Predicate.And) {
                false
            } else {
                pathConstraintsInCurrentPoint.predicates.containsAll(locationConstraints.predicates)
            }
        }

        pathConstraintsInCurrentPoint is Predicate.And -> pathConstraintsInCurrentPoint.predicates.contains(locationConstraints)
        else -> false
    }
}