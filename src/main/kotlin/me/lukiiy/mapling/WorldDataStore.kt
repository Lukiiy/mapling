package me.lukiiy.mapling

import java.io.File

interface WorldDataStore {
    companion object {
        const val INCOMPATIBLE = "Unsupported value."
    }

    fun load(file: File): WorldData
    fun save(file: File, data: WorldData)
}