package me.lukiiy.mapling

import java.io.File

/**
 * An adapter for platforms!
 * @param W Your World object.
 */
interface WorldAdapter<W, L> {
    fun load(folder: File): W?

    fun save(world: W)

    fun unload(world: W): Boolean

    fun getLocation(world: W, position: Position): L
}