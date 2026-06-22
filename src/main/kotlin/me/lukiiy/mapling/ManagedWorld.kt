package me.lukiiy.mapling

import java.io.File

data class ManagedWorld<W>(val id: String, val folder: File, var handle: W? = null, var data: WorldData = WorldData()) {
    val dataFile: File
        get() = File(folder, "mapling.toml")
}
