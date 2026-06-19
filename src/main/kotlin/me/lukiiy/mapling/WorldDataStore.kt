package me.lukiiy.mapling

import java.io.File

interface WorldDataStore {
    fun load(file: File): WorldData
    fun save(file: File, data: WorldData)
}