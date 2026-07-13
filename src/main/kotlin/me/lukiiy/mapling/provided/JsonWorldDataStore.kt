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
import kotlin.collections.iterator
import kotlin.collections.map

class JsonWorldDataStore : WorldDataStore {
    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        return deserialize(Json.parseToJsonElement(file.readText()).jsonObject)
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()
        file.writeText(Json.encodeToString(JsonObject.serializer(), serialize(data)))
    }

    private fun serialize(data: WorldData): JsonObject {
        fun encode(value: Any): JsonElement = when (value) {
            is Position -> JsonPrimitive(value.serialize())
            is Boolean -> JsonPrimitive(value)
            is Long -> JsonPrimitive(value)
            is Int -> JsonPrimitive(value)
            is Double -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is List<*> -> JsonArray(value.map { encode(requireNotNull(it)) })
            else -> error("Unsupported value.")
        }

        return JsonObject(buildMap {
            for ((key, value) in data.values()) put(key, encode(value))
            for ((key, section) in data.sections()) put(key, serialize(section))
        })
    }

    private fun deserialize(obj: JsonObject): WorldData {
        fun decode(element: JsonElement): Any = when (element) {
            is JsonPrimitive -> when {
                element.content.startsWith("pos:") -> Position.deserialize(element.content)
                !element.isString -> element.booleanOrNull ?: element.longOrNull ?: element.doubleOrNull ?: element.content
                else -> element.content
            }

            is JsonArray -> element.map(::decode)
            else -> error("Unsupported value.")
        }

        fun read(obj: JsonObject, target: WorldData) {
            for ((key, element) in obj) {
                when (element) {
                    is JsonObject -> read(element, target.section(key))
                    else -> target.set(key, decode(element))
                }
            }
        }

        return WorldData().also { read(obj, it) }
    }
}