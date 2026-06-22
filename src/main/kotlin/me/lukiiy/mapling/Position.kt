package me.lukiiy.mapling

import java.io.Serializable

data class Position(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f) : Serializable {
    fun serialize(): String = "$x;$y;$z;$yaw;$pitch"

    companion object {
        fun deserialize(value: String): Position {
            val parts = value.split(';')
            if (parts.size != 5) throw IllegalArgumentException("Invalid position.")

            return Position(parts[0].toDouble(), parts[1].toDouble(), parts[2].toDouble(), parts[3].toFloat(), parts[4].toFloat())
        }
    }
}