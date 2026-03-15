package de.c4vxl.ranksystem.data

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * Data class representing a rank
 * @param name The name of the rank
 * @param position The position in tab list
 * @param prefix The prefix prepended to the name of players with this rank
 * @param suffix The suffix appended to the name of players with this rank
 * @param playerIDs The ids of the players that have this rank
 * @param permissions The permissions this rank grants
 */
data class Rank(
    val name: String,
    var position: String = "000",
    var prefix: String = "",
    var suffix: String = "",
    var playerIDs: MutableList<UUID> = mutableListOf(),
    var permissions: MutableList<String> = mutableListOf()
) {
    val players: List<OfflinePlayer>
        get() = playerIDs.map { Bukkit.getOfflinePlayer(it) }

    /**
     * Saves changes to database
     */
    fun save() =
        RankDB.update(this)

    /**
     * Apply changes to this rank and save them automatically
     * @param block The changes to apply
     */
    inline fun update(block: Rank.() -> Unit): Boolean {
        this.apply(block)
        return save()
    }
}