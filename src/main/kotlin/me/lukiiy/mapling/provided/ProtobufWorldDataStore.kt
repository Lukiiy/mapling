package me.lukiiy.mapling.provided

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import me.lukiiy.mapling.Position
import me.lukiiy.mapling.WorldData
import me.lukiiy.mapling.WorldDataStore
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
class ProtobufWorldDataStore : WorldDataStore {
    @Serializable
    sealed class BufValue {
        @Serializable @SerialName("b") data class BoolVal(val v: Boolean) : BufValue()
        @Serializable @SerialName("l") data class LongVal(val v: Long) : BufValue()
        @Serializable @SerialName("d") data class DoubleVal(val v: Double) : BufValue()
        @Serializable @SerialName("s") data class StringVal(val v: String) : BufValue()
        @Serializable @SerialName("a") data class ListVal(val v: List<BufValue>) : BufValue()
    }

    @Serializable
    data class ProtoArea(val from: String, val to: String)

    @Serializable
    data class WorldDataProto(val values: Map<String, BufValue> = emptyMap(), val positions: Map<String, String> = emptyMap(), val areas: Map<String, ProtoArea> = emptyMap(), val groups: Map<String, List<String>> = emptyMap())

    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        val proto = ProtoBuf.decodeFromByteArray(WorldDataProto.serializer(), file.readBytes())
        val data = WorldData()

        proto.values.forEach { (k, v) -> data.set(k, decode(v)) }
        proto.positions.forEach { (k, v) -> data.setPosition(k, Position.deserialize(v)) }
        proto.areas.forEach { (k, a) -> data.setArea(k, Position.deserialize(a.from), Position.deserialize(a.to)) }
        proto.groups.forEach { (k, list) ->
            val group = data.group(k)

            list.forEach { group.add(Position.deserialize(it)) }
        }

        return data
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()

        val proto = WorldDataProto(
            values = data.values().mapValues { (_, v) -> encode(v) },
            positions = data.positionValues().mapValues { (_, p) -> p.serialize() },
            areas = data.areaValues().mapValues { (_, a) -> ProtoArea(a.first.serialize(), a.second.serialize()) },
            groups = data.groups().filterValues { it.isNotEmpty() }.mapValues { (_, list) -> list.map { it.serialize() } }
        )

        file.writeBytes(ProtoBuf.encodeToByteArray(WorldDataProto.serializer(), proto))
    }

    private fun encode(value: Any): BufValue = when (value) {
        is Boolean -> BufValue.BoolVal(value)
        is Long -> BufValue.LongVal(value)
        is Double -> BufValue.DoubleVal(value)
        is String -> BufValue.StringVal(value)
        is List<*> -> BufValue.ListVal(value.map { encode(requireNotNull(it)) })
        else -> error(WorldDataStore.INCOMPATIBLE)
    }

    private fun decode(value: BufValue): Any = when (value) {
        is BufValue.BoolVal -> value.v
        is BufValue.LongVal -> value.v
        is BufValue.DoubleVal -> value.v
        is BufValue.StringVal -> value.v
        is BufValue.ListVal -> value.v.map(::decode)
    }
}