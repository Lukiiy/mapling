package me.lukiiy.mapling

data class Position(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f) {
    fun serialize(): String = "pos:$x;$y;$z;$yaw;$pitch"

    companion object {
        fun deserialize(value: String): Position {
            require(value.startsWith("pos:")) { "Invalid position." }

            val parts = value.substring(4).split(';')
            if (parts.size != 5) throw IllegalArgumentException("Invalid position.")

            return Position(parts[0].toDouble(), parts[1].toDouble(), parts[2].toDouble(), parts[3].toFloat(), parts[4].toFloat())
        }
    }
}