package me.lukiiy.mapling

import java.io.File

/**
 * An interface to create all sorts of files to save/load a [WorldData]!
 */
interface WorldDataStore {
    companion object {
        const val INCOMPATIBLE = "Unsupported value."
    }

    /**
     * Decode a [WorldData] from a given file
     * @param file The file
     * @return A data holder
     */
    fun load(file: File): WorldData

    /**
     * Encode a given [WorldData] into a file
     * @param file The file that will be used
     * @param data The data
     */
    fun save(file: File, data: WorldData)
}