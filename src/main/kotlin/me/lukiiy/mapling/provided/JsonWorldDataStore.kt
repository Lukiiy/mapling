package me.lukiiy.mapling.provided

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import me.lukiiy.mapling.Position
import me.lukiiy.mapling.WorldData
import me.lukiiy.mapling.WorldDataStore
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.map

class JsonWorldDataStore : WorldDataStore {
    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        val root = Json.parseToJsonElement(file.readText()).jsonObject
        val data = WorldData()

        // parsings & gathering!
        (root["values"] as? JsonObject)?.forEach { (k, v) -> data.set(k, decode(v)) }
        (root["positions"] as? JsonObject)?.forEach { (k, v) -> data.setPosition(k, Position.deserialize((v as JsonPrimitive).content)) }

        (root["areas"] as? JsonObject)?.forEach { (k, v) ->
            val value = v as JsonObject

            data.setArea(k, Position.deserialize((value["from"] as JsonPrimitive).content), Position.deserialize((value["to"] as JsonPrimitive).content))
        }

        (root["groups"] as? JsonObject)?.forEach { (k, v) ->
            val list = data.group(k)

            (v as JsonArray).forEach { list.add(Position.deserialize((it as JsonPrimitive).content)) }
        }

        return data
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()

        val root = buildMap<String, JsonElement> {
            data.values().takeIf { it.isNotEmpty() }?.let {
                put("values", JsonObject(it.mapValues { (_, v) -> encode(v) }))
            }

            data.positionValues().takeIf { it.isNotEmpty() }?.let {
                put("positions", JsonObject(it.mapValues { (_, p) -> JsonPrimitive(p.serialize()) }))
            }

            data.areaValues().takeIf { it.isNotEmpty() }?.let { areas ->
                put("areas", JsonObject(areas.mapValues { (_, a) ->
                    JsonObject(mapOf("from" to JsonPrimitive(a.first.serialize()), "to" to JsonPrimitive(a.second.serialize())))
                }))
            }

            data.groups().filterValues { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.let { groups ->
                put("groups", JsonObject(groups.mapValues { (_, list) ->
                    JsonArray(list.map { JsonPrimitive(it.serialize()) })
                }))
            }
        }

        file.writeText(Json.encodeToString(JsonObject.serializer(), JsonObject(root)))
    }

    private fun encode(value: Any): JsonElement = when (value) {
        is Boolean -> JsonPrimitive(value)
        is Long -> JsonPrimitive(value)
        is Double -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is List<*> -> JsonArray(value.map { encode(requireNotNull(it)) })
        else -> error(WorldDataStore.INCOMPATIBLE)
    }

    private fun decode(element: JsonElement): Any = when (element) {
        is JsonPrimitive -> when {
            !element.isString -> element.booleanOrNull ?: element.longOrNull ?: element.doubleOrNull ?: element.content

            else -> element.content
        }

        is JsonArray -> element.map(::decode)

        else -> error(WorldDataStore.INCOMPATIBLE)
    }
}