package me.lukiiy.mapling

import dev.eav.tomlkt.TomlArray
import dev.eav.tomlkt.TomlLiteral
import dev.eav.tomlkt.TomlTable
import dev.eav.tomlkt.TomlTableBuilder
import dev.eav.tomlkt.array
import dev.eav.tomlkt.buildTomlTable
import dev.eav.tomlkt.literal
import dev.eav.tomlkt.table
import dev.eav.tomlkt.toBooleanOrNull
import dev.eav.tomlkt.toDoubleOrNull
import dev.eav.tomlkt.toLongOrNull

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

    // convenience??
    fun getLong(key: String): Long? = values[key] as? Long
    fun getDouble(key: String): Double? = values[key] as? Double
    fun getInt(key: String): Int? = getLong(key)?.toInt()
    fun getFloat(key: String): Float? = getDouble(key)?.toFloat()

    fun remove(key: String): Any? = values.remove(key)

    fun clear() {
        values.clear()
        sections.clear()
    }

    fun contains(key: String): Boolean = values.containsKey(key) || sections.containsKey(key)

    fun keys(): Set<String> = values.keys
    fun sectionKeys(): Set<String> = sections.keys
    fun values(): Map<String, Any?> = values.toMap()
    fun sections(): Map<String, WorldData> = sections.toMap()

    // Toml
    // TODO: OH MY. OH MY. HOW DO I NOT BREAK THIS?
    fun toToml(): TomlTable = buildTomlTable {
        fun TomlTableBuilder.write(data: WorldData) {
            for ((key, value) in data.values()) {
                when (value) {
                    is String -> literal(key, value)
                    is Boolean -> literal(key, value)
                    is Long -> literal(key, value)
                    is Double -> literal(key, value)
                    is Position -> literal(key, value.serialize())
                    is List<*> -> array(key) {
                        for (item in value) {
                            when (item) {
                                is String -> literal(item)
                                is Boolean -> literal(item)
                                is Long -> literal(item)
                                is Double -> literal(item)
                                is Position -> literal(item.serialize())
                                else -> error("Unsupported value.")
                            }
                        }
                    }
                    else -> error("Unsupported value.")
                }
            }

            for ((key, section) in data.sections()) table(key) { write(section) }
        }

        write(this@WorldData)
    }

    companion object {
        fun fromToml(table: TomlTable): WorldData {
            fun TomlLiteral.toValue(): Any {
                val text = content

                return toBooleanOrNull() ?: toLongOrNull() ?: toDoubleOrNull() ?: if (text.startsWith("pos:")) Position.deserialize(text) else text
            }

            fun read(source: TomlTable, target: WorldData) {
                for ((key, value) in source) {
                    when (value) {
                        is TomlLiteral -> target.set(key, value.toValue())
                        is TomlArray -> target.set(key, value.map {
                                when (it) {
                                    is TomlLiteral -> it.toValue()
                                    else -> error("Unsupported value.")
                                }
                            })
                        is TomlTable -> read(value, target.section(key))
                        else -> error("Unsupported value.")
                    }
                }
            }

            return WorldData().also { read(table, it) }
        }
    }
}