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
    data class ProtoPosition(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f)

    @Serializable
    data class ProtoVec3(val x: Double, val y: Double, val z: Double)

    @Serializable
    data class ProtoArea(val from: ProtoVec3, val to: ProtoVec3)

    @Serializable
    data class WorldDataProto(val values: Map<String, BufValue> = emptyMap(), val positions: Map<String, ProtoPosition> = emptyMap(), val areas: Map<String, ProtoArea> = emptyMap(), val groups: Map<String, List<ProtoPosition>> = emptyMap())

    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        val proto = ProtoBuf.decodeFromByteArray(WorldDataProto.serializer(), file.readBytes())
        val data = WorldData()

        proto.values.forEach { (k, v) -> data.set(k, decode(v)) }
        proto.positions.forEach { (k, p) -> data.setPosition(k, Position(p.x, p.y, p.z, p.yaw, p.pitch)) }
        proto.areas.forEach { (k, a) -> data.setArea(k, Position(a.from.x, a.from.y, a.from.z), Position(a.to.x, a.to.y, a.to.z)) }
        proto.groups.forEach { (k, list) -> list.forEach { p -> data.group(k).add(Position(p.x, p.y, p.z, p.yaw, p.pitch)) } }

        return data
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()

        val proto = WorldDataProto(
            values = data.values().mapValues { (_, v) -> encode(v) },
            positions = data.positionValues().mapValues { (_, p) -> ProtoPosition(p.x, p.y, p.z, p.yaw, p.pitch) },

            areas = data.areaValues().mapValues { (_, a) -> ProtoArea(ProtoVec3(a.first.x, a.first.y, a.first.z), ProtoVec3(a.second.x, a.second.y, a.second.z)) },

            groups = data.groups().filterValues { it.isNotEmpty() }
                .mapValues { (_, list) ->
                    list.map { p -> ProtoPosition(p.x, p.y, p.z, p.yaw, p.pitch) }
                }
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