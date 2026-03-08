package de.c4vxl.ranksystem.utils

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object ResourceUtils {
    /**
     * Reads the content of a jar-packed resource
     */
    fun readResource(path: String): String =
        ResourceUtils::class.java.getResourceAsStream("/$path")?.bufferedReader()?.readText() ?: ""

    /**
     * Saves a jar-packed resource
     * @param path The path to the resource
     * @param destination The destination path
     */
    fun saveResource(path: String, destination: String, replace: Boolean = false) {
        val destPath = Path.of(destination)
        destPath.parent?.toFile()?.mkdirs()

        if (replace) Files.deleteIfExists(destPath)

        if (destPath.exists())
            return

        ResourceUtils::class.java.getResourceAsStream("/$path")
            ?.use { Files.copy(it, destPath) }
            ?: error("Resource $path not found!")
    }
}