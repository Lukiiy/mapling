package me.lukiiy.mapling

import java.io.File

/**
 * An adapter for platforms!
 * @param W Your World object.
 */
interface WorldAdapter<W> {
    fun loadWorld(folder: File): W?
    fun saveWorld(world: W)
    fun unloadWorld(world: W): Boolean
}