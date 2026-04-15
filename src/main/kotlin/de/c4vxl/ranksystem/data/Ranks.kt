package de.c4vxl.ranksystem.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.event.data.RankDataChangeEvent
import de.c4vxl.ranksystem.event.data.RankRegisterEvent
import de.c4vxl.ranksystem.event.data.RankUnregisterEvent
import org.bukkit.Bukkit
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object Ranks {
    val cache = ConcurrentHashMap<String, RankCache?>()

    /**
     * Returns the database directory
     */
    private val dbDir: File get() =
        File(Main.config.getString("config.db-path") ?: "./ranks/")
            .also { it.mkdirs() }

    /**
     * Holds a gson instance
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .apply {
                if (Main.config.getBoolean("config.db-pretty-print")) this.setPrettyPrinting()
            }
            .create()
    }

    init {
        // Load all ranks into cache
        dbDir.listFiles { f -> f.extension == "rank" }?.forEach { get(it.nameWithoutExtension) }

        // Register cache autosave
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance, Runnable {
            saveAll()
        }, 0, Main.config.getInt("config.cache-save-interval", 18000) * 20L)
    }

    /**
     * Returns a list of all registered ranks
     */
    val registeredRanks: Map<String, Rank>
        get() = cache.values.mapNotNull {
            val data = it?.data ?: return@mapNotNull null
            return@mapNotNull data.name to data
        }.toMap()

    /**
     * Holds the default rank cached
     */
    private var defaultRank: String? = Main.config.getString("config.default")

    /**
     * Returns the default rank
     */
    fun getDefaultRank() = defaultRank?.let { get(it) }

    /**
     * Sets the default rank
     * @param rank The rank to make the default
     */
    fun setDefaultRank(rank: String) {
        Main.config.set("config.default", rank)
        Main.config.save(Main.instance.dataFolder.resolve("config.yml"))
        defaultRank = rank
    }

    /**
     * Fetches a rank cache or disk and caches it
     * @param
     */
    fun get(name: String): Rank? =
        cache.computeIfAbsent(name) {
            val file = dbDir.resolve("$name.rank")
            if (!file.exists()) return@computeIfAbsent null

            return@computeIfAbsent RankCache(gson.fromJson(file.readText(), Rank::class.java))
        }?.data

    /**
     * Sets a value in cache
     * @param name The key in cache
     * @param rank The object to store (null to remove)
     */
    fun set(name: String, rank: Rank?) {
        if (rank == null)
            cache.remove(name)
        else
            cache[name] = RankCache(rank, true)
    }

    /**
     * Registers a rank in db
     * @param rank The rank to register
     */
    fun register(rank: Rank): Boolean {
        if (exists(rank.name))
            return false

        // Register rank
        set(rank.name, rank)

        // Call event
        RankRegisterEvent(rank).callEvent()

        return true
    }

    /**
     * Unregisters a rank in db
     * @param name The name of the rank to unregister
     */
    fun unregister(name: String): Boolean {
        val rank = get(name) ?: return false

        // Call event
        RankUnregisterEvent(rank).callEvent()

        // Register rank
        set(name, null)

        return true
    }

    /**
     * Applies a change to a specific rank
     * @param name The rank to apply the change to
     * @param update The change to apply
     */
    inline fun modify(name: String, update: Rank.() -> Unit): Boolean {
        val rank = get(name) ?: return false
        rank.apply(update)
        makeDirty(name)
        return true
    }

    /**
     * Saves a cached rank to disk
     * @param name The name of the cache to save
     */
    fun save(name: String) {
        val cached = cache[name] ?: return

        // Cache is not dirty
        if (!cached.isDirty)
            return

        // Save in db
        dbDir.resolve("${name}.rank")
            .writeText(gson.toJson(cached.data))
    }

    /**
     * Saves the entire cache to disk
     */
    fun saveAll() {
        Main.logger.info("Saving cache to disk...")
        cache.keys.forEach { save(it) }
    }

    /**
     * Marks a specific rank in cache as dirty
     */
    fun makeDirty(rank: String) =
        cache[rank]?.makeDirty()

    /**
     * Returns {@code true} if a rank exists in cache
     * @param rank The rank to check
     */
    fun exists(rank: String) =
        cache.containsKey(rank)
}