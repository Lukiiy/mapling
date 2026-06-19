package me.lukiiy.mapling

import java.io.Serializable

data class Location(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f, ) : Serializable
