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
    private val gson: Gson by lazy {
        GsonBuilder()
            .apply {
                if (isPrettyPrint) this.setPrettyPrinting()
            }
            .create()
    }

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
     * Returns a rank object form it's db-name
     * @param rank The name of the rank
     */
    fun getRank(rank: String): Rank? {
        val file = dbDir.resolve("$rank.rank")
        if (!file.exists()) return null
        return gson.fromJson(file.readText(), Rank::class.java)
    }

    /**
     * Apply changes to a given rank and save them
     * @param name The name of the rank
     * @param block The changes to apply
     */
    inline fun rank(name: String, block: Rank.() -> Unit): Boolean {
        val rank = getRank(name) ?: return false
        rank.apply(block)
        return update(rank)
    }

    /**
     * Returns a list of all registered rank names
     */
    val registeredRanks: Map<String, Rank>
        get() = buildMap {
            dbDir.listFiles { f -> f.extension == "rank" }?.forEach {
                put(
                    it.nameWithoutExtension,
                    gson.fromJson(it.readText(), Rank::class.java)
                )
            }
        }

    /**
     * Registers a rank to the db
     * @param rank The rank to register
     * @return Returns {@code true} if the action was successful
     */
    fun registerRank(rank: Rank): Boolean {
        val file = dbDir.resolve("${rank.name}.rank")

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
        val rankObject = getRank(rank) ?: return false

        // Delete db entry
        if (!dbDir.resolve("$rank.rank").delete())
            return false

        // Trigger event
        RankUnregisterEvent(rankObject).callEvent()

        return true
    }

    /**
     * Updates a rank in db
     * @param rank The updated rank
     */
    fun update(rank: Rank): Boolean {
        val oldRank = getRank(rank.name) ?: return false

        // Write to db
        dbDir.resolve("${rank.name}.rank")
            .writeText(gson.toJson(rank))

        // Trigger events
        RankDBSaveEvent(rank).callEvent()

        if (oldRank.position != rank.position)
            RankPositionUpdateEvent(rank, oldRank.position, rank.position).callEvent()

        if (oldRank.prefix != rank.prefix)
            RankPrefixUpdateEvent(rank, oldRank.prefix, rank.prefix).callEvent()

        if (oldRank.suffix != rank.suffix)
            RankSuffixUpdateEvent(rank, oldRank.suffix, rank.suffix).callEvent()

        val oldPerms = oldRank.permissions.toSet()
        val newPerms = rank.permissions.toSet()
        (oldPerms - newPerms).forEach { RankPermissionsUpdateEvent(rank, it, false).callEvent() }
        (newPerms - oldPerms).forEach { RankPermissionsUpdateEvent(rank, it, true).callEvent() }

        return true
    }

    /**
     * Returns {@code true} if the provided rank is already registered
     * @param rank The name of the rank
     */
    fun rankExists(rank: String): Boolean =
        dbDir.resolve("$rank.rank").exists()
}