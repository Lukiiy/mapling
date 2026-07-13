package me.lukiiy.mapling

/**
 * Data holder for a [ManagedWorld]. Can hold positions, areas and cusotm values.
 */
class WorldData(private val values: MutableMap<String, Any> = linkedMapOf(), private val sections: MutableMap<String, WorldData> = linkedMapOf(), private val positions: MutableMap<String, Position> = linkedMapOf(), private val areas: MutableMap<String, Pair<Position, Position>> = linkedMapOf()) {
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
     * Gets or creates a nested [WorldData] section, serving as sort of a group.
     *
     * @param name Dot-separated path, (like "spawns.overworld")
     * @return The section at that path (created if absent)
     */
    fun section(name: String): WorldData {
        val parts = name.split('.').filter { it.isNotBlank() }
        var current = this

        for (part in parts) current = current.sections.getOrPut(part) { WorldData() }

        return current
    }

    /**
     * Gets the nested [WorldData] section at the given dot-separated path, if it exists. Does not create missing sections.
     *
     * @param name Dot-separated path, (like "spawns.overworld")
     * @return The section at that path, or `null` if any part of the path doesn't exist
     */
    fun getSection(name: String): WorldData? {
        val parts = name.split('.').filter { it.isNotBlank() }
        var current: WorldData = this

        for (part in parts) current = current.sections[part] ?: return null

        return current
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = values[key] as? T

    fun remove(key: String): Any? = values.remove(key)

    fun clear() {
        values.clear()
        sections.clear()
        positions.clear()
        areas.clear()
    }

    fun contains(key: String): Boolean = values.containsKey(key) || sections.containsKey(key) || positions.containsKey(key) || areas.containsKey(key)

    fun keys(): Set<String> = values.keys
    fun sectionKeys(): Set<String> = sections.keys
    fun positionKeys(): Set<String> = positions.keys
    fun areaKeys(): Set<String> = areas.keys

    fun values(): Map<String, Any> = values.toMap()
    fun sections(): Map<String, WorldData> = sections.toMap()
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

    fun isEmpty(): Boolean = values.isEmpty() && sections.isEmpty() && positions.isEmpty() && areas.isEmpty()
}