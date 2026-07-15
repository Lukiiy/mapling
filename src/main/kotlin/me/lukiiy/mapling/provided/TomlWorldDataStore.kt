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
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.map

class TomlWorldDataStore : WorldDataStore {
    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        val root = Toml.parseToTomlTable(file.readText())
        val data = WorldData()

        // parsings & gathering!
        (root["values"] as? TomlTable)?.forEach { (k, v) -> data.set(k, decode(v)) }
        (root["positions"] as? TomlTable)?.forEach { (k, v) -> data.setPosition(k, Position.deserialize((v as TomlLiteral).content)) }

        (root["areas"] as? TomlTable)?.forEach { (k, v) ->
            val value = v as TomlTable

            data.setArea(k, Position.deserialize((value["from"] as TomlLiteral).content), Position.deserialize((value["to"] as TomlLiteral).content))
        }

        (root["groups"] as? TomlTable)?.forEach { (k, v) ->
            val list = data.group(k)

            (v as TomlArray).forEach { list.add(Position.deserialize((it as TomlLiteral).content)) }
        }

        return data
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()

        val root = buildMap<String, TomlElement> {
            data.values().takeIf { it.isNotEmpty() }?.let {
                put("values", TomlTable(it.mapValues { (_, v) -> encode(v) }))
            }

            data.positionValues().takeIf { it.isNotEmpty() }?.let {
                put("positions", TomlTable(it.mapValues { (_, p) -> TomlLiteral(p.serialize()) }))
            }

            data.areaValues().takeIf { it.isNotEmpty() }?.let { areas ->
                put("areas", TomlTable(areas.mapValues { (_, a) ->
                    TomlTable(mapOf("from" to TomlLiteral(a.first.serialize()), "to" to TomlLiteral(a.second.serialize())))
                }))
            }

            data.groups().filterValues { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.let { groups ->
                put("groups", TomlTable(groups.mapValues { (_, list) ->
                    TomlArray(list.map { TomlLiteral(it.serialize()) })
                }))
            }
        }

        file.writeText(Toml.encodeToString(TomlTable(root)))
    }

    private fun encode(value: Any): TomlElement = when (value) {
        is Boolean -> TomlLiteral(value)
        is Long -> TomlLiteral(value)
        is Double -> TomlLiteral(value)
        is String -> TomlLiteral(value)
        is List<*> -> TomlArray(value.map { encode(requireNotNull(it)) })
        else -> error(WorldDataStore.INCOMPATIBLE)
    }

    private fun decode(element: TomlElement): Any = when (element) {
        is TomlLiteral -> element.toBooleanOrNull() ?: element.toLongOrNull() ?: element.toDoubleOrNull() ?: element.content

        is TomlArray -> element.map(::decode)

        else -> error(WorldDataStore.INCOMPATIBLE)
    }
}