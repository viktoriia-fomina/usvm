package equals

class Team(val name: String, val members: List<String>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Team) return false

        return name == other.name && members.toSet() == other.members.toSet()
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + members.toSet().hashCode()
        return result
    }
}
