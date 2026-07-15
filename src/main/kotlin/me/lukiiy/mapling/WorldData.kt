package me.lukiiy.mapling

/**
 * Data holder for a [ManagedWorld]. Can hold positions, areas and cusotm values.
 */
class WorldData(private val values: MutableMap<String, Any> = linkedMapOf(), private val positions: MutableMap<String, Position> = linkedMapOf(), private val areas: MutableMap<String, Pair<Position, Position>> = linkedMapOf(), private val groups: MutableMap<String, MutableList<Position>> = linkedMapOf()) {
    fun set(key: String, value: Any): WorldData {
        if (value is Position) return setPosition(key, value)

        values[key] = normalize(value)
        return this
    }

    internal fun normalize(value: Any): Any {
        return when (value) {
            is Byte -> value.toLong()
            is Short -> value.toLong()
            is Int -> value.toLong()
            is Float -> value.toDouble()
            is Long -> value
            is Double -> value
            is Boolean -> value
            is String -> value
            is Position -> value
            is List<*> -> value.map {
                requireNotNull(it) { "Null not supported!" }

                normalize(it)
            }
            
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.qualifiedName}")
        }
    }

    /**
     * Gets or creates the position group [name].
     * @param name The group name
     * @return The group with the given name (created if absent)
     */
    fun group(name: String): MutableList<Position> = groups.getOrPut(name) { mutableListOf() }

    /**
     * A snapshot of all available groups, keyed by name!
     */
    fun groups(): Map<String, List<Position>> = groups.mapValues { it.value.toList() }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = values[key] as? T

    fun remove(key: String): Any? = values.remove(key)

    fun clear() {
        values.clear()
        positions.clear()
        areas.clear()
        groups.clear()
    }

    fun contains(key: String): Boolean = values.containsKey(key) || positions.containsKey(key) || areas.containsKey(key) || groups.containsKey(key)

    fun keys(): Set<String> = values.keys
    fun groupKeys(): Set<String> = groups.keys
    fun positionKeys(): Set<String> = positions.keys
    fun areaKeys(): Set<String> = areas.keys

    fun values(): Map<String, Any> = values.toMap()
    fun positionValues(): Map<String, Position> = positions.toMap()
    fun areaValues(): Map<String, Pair<Position, Position>> = areas.toMap()

    // Positions
    fun setPosition(key: String, pos: Position): WorldData {
        positions[key] = pos
        return this
    }

    fun getPosition(key: String): Position? = positions[key]

    // Areas (Position pairs)
    fun setArea(name: String, from: Position, to: Position): WorldData {
        areas[name] = from to to
        return this
    }

    fun getArea(name: String): Pair<Position, Position>? = areas[name]

    fun isEmpty(): Boolean = values.isEmpty() && positions.isEmpty() && areas.isEmpty() && groups.isEmpty()
}