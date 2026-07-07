package me.lukiiy.mapling

class WorldData(private val values: MutableMap<String, Any> = linkedMapOf(), private val sections: MutableMap<String, WorldData> = linkedMapOf()) {
    fun set(key: String, value: Any): WorldData {
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
                requireNotNull(it) { "Null values are not supported in Mapling lists." }

                normalize(it)
            }
            
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.qualifiedName}")
        }
    }

    fun section(name: String): WorldData {
        val parts = name.split('.').filter { it.isNotBlank() }
        var current = this

        for (part in parts) current = current.sections.getOrPut(part) { WorldData() }

        return current
    }

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
    }

    fun contains(key: String): Boolean = values.containsKey(key) || sections.containsKey(key)

    fun keys(): Set<String> = values.keys
    fun sectionKeys(): Set<String> = sections.keys
    fun values(): Map<String, Any> = values.toMap()
    fun sections(): Map<String, WorldData> = sections.toMap()

    // Areas
    fun setArea(name: String, from: Position, to: Position): WorldData {
        section(name).set("from", from).set("to", to)
        return this
    }

    fun getArea(name: String): Pair<Position, Position>? {
        val section = getSection(name) ?: return null

        return Pair(section.get<Position>("from") ?: return null, section.get<Position>("to") ?: return null)
    }

    fun isEmpty(): Boolean = values().isEmpty() && sections().isEmpty()
}