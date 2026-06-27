package me.lukiiy.mapling.provided

import dev.eav.tomlkt.Toml
import kotlinx.serialization.encodeToString
import me.lukiiy.mapling.WorldData
import me.lukiiy.mapling.WorldDataStore
import java.io.File

class TomlWorldDataStore : WorldDataStore {
    override fun load(file: File): WorldData {
        if (!file.exists()) return WorldData()

        return WorldData.fromToml(Toml.parseToTomlTable(file.readText()))
    }

    override fun save(file: File, data: WorldData) {
        if (data.isEmpty()) return

        file.parentFile?.mkdirs()
        file.writeText(Toml.encodeToString(data.toToml()))
    }
}