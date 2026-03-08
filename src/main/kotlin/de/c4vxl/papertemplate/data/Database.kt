package de.c4vxl.papertemplate.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.c4vxl.papertemplate.Main
import de.c4vxl.papertemplate.event.data.RankDBSaveEvent
import de.c4vxl.papertemplate.event.data.RankRegisterEvent
import de.c4vxl.papertemplate.event.data.RankUnregisterEvent
import de.c4vxl.papertemplate.event.update.RankPermissionsUpdateEvent
import de.c4vxl.papertemplate.event.update.RankPositionUpdateEvent
import de.c4vxl.papertemplate.event.update.RankPrefixUpdateEvent
import de.c4vxl.papertemplate.event.update.RankSuffixUpdateEvent
import io.leangen.geantyref.TypeToken
import java.io.File

/**
 * Central database
 */
object Database {
    /**
     * Returns the directory where the database is stored
     */
    val dbDir: File
        get() = File(
            Main.config.getString("config.db-path") ?: "./ranks/"
        ).also {
            it.mkdirs()
        }

    /**
     * Returns {@code true} if pretty print should be used
     */
    val isPrettyPrint: Boolean
        get() = Main.config.getBoolean("config.db-pretty-print")

    /**
     * Returns a gson instance
     */
    val gson: Gson
        get() = GsonBuilder()
            .let {
                if (isPrettyPrint)
                    it.setPrettyPrinting()
                else
                    it
            }
            .create()

    /**
     * Holds the default rank a player will receive when joining for the first time
     */
    var defaultRank: String?
        get() = Main.config.getString("config.default")
        set(value) {
            Main.config.set("config.default", value)
            Main.config.save(Main.instance.dataFolder.resolve("config.yml"))
        }

    /**
     * Returns a list of all registered rank names
     */
    val registeredRanks: Map<String, Rank>
        get() = buildMap {
            dbDir.listFiles()?.forEach {
                put(
                    it.nameWithoutExtension,
                    gson.fromJson(it.readText(), object : TypeToken<Rank>() {}.type)
                )
            }
        }

    /**
     * Registers a rank to the db
     * @param rank The rank to register
     * @return Returns {@code true} if the action was successful
     */
    fun registerRank(rank: Rank): Boolean {
        val file = dbDir.resolve(rank.name)

        // Try to create db file
        // If file exists, Rank already registered
        // Panic and exit
        if (!file.createNewFile())
            return false

        // Write to config
        file.writeText(gson.toJson(rank))

        // Trigger event
        RankRegisterEvent(rank).callEvent()

        return true
    }

    /**
     * Unregisters a rank from db
     * @param rank The name of the rank to unregister
     * @return Returns {@code true} if the action was successful
     */
    fun unregisterRank(rank: String): Boolean {
        val rankObject = registeredRanks[rank] ?: return false

        // Delete db entry
        dbDir.resolve(rank).delete()

        // Trigger event
        RankUnregisterEvent(rankObject).callEvent()

        return true
    }

    /**
     * Updates a rank in db
     * @param rank The updated rank
     */
    fun update(rank: Rank): Boolean {
        val oldRank = registeredRanks[rank.name] ?: return false

        // Write to db
        dbDir.resolve(rank.name)
            .writeText(gson.toJson(rank))

        // Trigger events
        RankDBSaveEvent(rank).callEvent()
        oldRank.let {
            if (it.position != rank.position)
                RankPositionUpdateEvent(rank, it.position, rank.position).callEvent()

            if (it.prefix != rank.prefix)
                RankPrefixUpdateEvent(rank, it.prefix, rank.prefix).callEvent()

            if (it.suffix != rank.suffix)
                RankSuffixUpdateEvent(rank, it.suffix, rank.suffix).callEvent()

            it.permissions.forEach { perm ->
                if (!rank.permissions.contains(perm))
                    RankPermissionsUpdateEvent(rank, perm, false).callEvent()
            }

            rank.permissions.forEach { perm ->
                if (!it.permissions.contains(perm))
                    RankPermissionsUpdateEvent(rank, perm, true).callEvent()
            }
        }

        return true
    }

    /**
     * Returns {@code true} if the provided rank is already registered
     * @param rank The name of the rank
     */
    fun rankExists(rank: String): Boolean =
        dbDir.resolve(rank).exists()
}