package me.lukiiy.mapling

import java.io.File

/**
 * A loaded or registered world entry.
 */
data class MinigameWorld<W>(val id: String, val folder: File, var handle: W? = null, var data: WorldData = WorldData()) {
    val dataFile: File get() = File(folder, "DATA.dat")
}