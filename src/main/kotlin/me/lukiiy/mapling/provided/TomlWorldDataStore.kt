package me.lukiiy.mapling.provided

import dev.eav.tomlkt.Toml
import dev.eav.tomlkt.TomlArray
import dev.eav.tomlkt.TomlElement
import dev.eav.tomlkt.TomlLiteral
import dev.eav.tomlkt.TomlTable
import dev.eav.tomlkt.toBooleanOrNull
import dev.eav.tomlkt.toDoubleOrNull
import dev.eav.tomlkt.toLongOrNull
import kotlinx.serialization.encodeToString
import me.lukiiy.mapling.Position
import me.lukiiy.mapling.WorldData
import me.lukiiy.mapling.WorldDataStore
import java.io.File

class TomlWorldDataStore : WorldDataStore {
    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        return deserialize(Toml.parseToTomlTable(file.readText()))
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()
        file.writeText(Toml.encodeToString(serialize(data)))
    }

    private fun serialize(data: WorldData): TomlTable {
        fun encode(value: Any): Any = when (value) {
            is Position -> value.serialize()
            is List<*> -> value.map {
                encode(requireNotNull(it))
            }

            else -> value
        }

        return TomlTable(buildMap {
            for ((key, value) in data.values()) put(key, encode(value))
            for ((key, section) in data.sections()) put(key, serialize(section))
        })
    }

    private fun deserialize(table: TomlTable): WorldData {
        fun decode(element: TomlElement): Any = when (element) {
            is TomlLiteral -> element.toBooleanOrNull() ?: element.toLongOrNull() ?: element.toDoubleOrNull() ?: element.content.takeIf { it.startsWith("pos:") }?.let(Position::deserialize) ?: element.content
            is TomlArray -> element.map(::decode)

            else -> error("Unsupported value.")
        }

        fun read(table: TomlTable, target: WorldData) {
            for ((key, element) in table) {
                when (element) {
                    is TomlTable -> read(element, target.section(key))
                    else -> target.set(key, decode(element))
                }
            }
        }

        return WorldData().also { read(table, it) }
    }
}