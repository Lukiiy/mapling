package me.lukiiy.mapling

import java.io.Serializable

/**
 * Arbitrary stored data for a world.
 */
class WorldData(val values: MutableMap<String, Any?> = linkedMapOf()) : Serializable {
    fun set(key: String, value: Any?): WorldData {
        values[key] = value
        return this
    }

    fun get(key: String): Any? = values[key]

    inline fun <reified T> getAs(key: String): T? = values[key] as? T

    fun remove(key: String): Any? = values.remove(key)

    fun clear() = values.clear()

    fun contains(key: String): Boolean = values.containsKey(key)

    fun keys(): Set<String> = values.keys

    fun asMap(): Map<String, Any?> = values.toMap()
}