package me.lukiiy.mapling

import me.lukiiy.mapling.provided.TomlWorldDataStore
import java.io.File

class WorldManager<W, L>(private val rootFolder: File, private val adapter: WorldAdapter<W, L>, private val dataStore: WorldDataStore = TomlWorldDataStore()) {
    private val worlds: MutableMap<String, ManagedWorld<W>> = linkedMapOf()

    fun register(id: String): ManagedWorld<W> {
        val folder = File(rootFolder, id)

        folder.mkdirs()

        return worlds.getOrPut(id) { ManagedWorld(id, folder) }
    }

    fun get(id: String): ManagedWorld<W>? = worlds[id]
    fun all(): Collection<ManagedWorld<W>> = worlds.values

    fun load(id: String): ManagedWorld<W> {
        val entry = register(id)

        if (!entry.dataFile.exists()) dataStore.save(entry.dataFile, entry.data)

        entry.data = dataStore.load(entry.dataFile)
        entry.handle = adapter.load(entry.folder)

        return entry
    }

    fun save(id: String) {
        val entry = worlds[id] ?: return

        entry.handle?.let(adapter::save)
        dataStore.save(entry.dataFile, entry.data)
    }

    fun saveAll() = worlds.keys.toList().forEach { save(it) }

    fun unload(id: String, saveFirst: Boolean = true): Boolean {
        val entry = worlds[id] ?: return false

        if (saveFirst) save(id)

        val handle = entry.handle

        val unloaded = if (handle != null) {
            adapter.unload(handle)
        } else {
            true
        }

        entry.handle = null
        worlds.remove(id)

        return unloaded
    }

    fun unloadAll(saveFirst: Boolean = true) = worlds.keys.toList().forEach { id -> unload(id, saveFirst) }
}