package de.c4vxl.ranksystem.player

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.data.Ranks
import de.c4vxl.ranksystem.event.update.RankPlayerAddEvent
import de.c4vxl.ranksystem.event.update.RankPlayerRemoveEvent
import de.c4vxl.ranksystem.language.Language
import de.c4vxl.ranksystem.language.Language.Companion.default
import de.c4vxl.ranksystem.language.Language.Companion.langsDB
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.file.YamlConfiguration

/**
 * A wrapper around the normal bukkit player
 * @param bukkitPLayer The bukkit player instance
 */
class RankPlayer(
    val bukkitPlayer: OfflinePlayer
) {
    companion object {
        val OfflinePlayer.rankPlayer
            get() = RankPlayer(this)
    }

    /**
     * Holds the language of the player
     */
    var language: Language
        get() {
            val lang = YamlConfiguration.loadConfiguration(langsDB)
                .getString(this.bukkitPlayer.uniqueId.toString()) ?: return default

            return Language.get(lang) ?: default
        }
        set(value) {
            YamlConfiguration.loadConfiguration(langsDB).apply {
                this.set(this@RankPlayer.bukkitPlayer.uniqueId.toString(), value.name)
                this.save(langsDB)
            }
        }

    /**
     * Returns a list of all ranks the player is a member of
     */
    val ranks: List<Rank>
        get() = Ranks.registeredRanks.values
            .filter { it.playerIDs.contains(this.bukkitPlayer.uniqueId) }
            .sortedBy { it.position }

    /**
     * Returns the highest rank of the player
     */
    val highestRank: Rank?
        get() = ranks.firstOrNull() ?: Ranks.getDefaultRank()

    /**
     * Returns {@code true} if a given rank is the players highest rank
     * @param rank The rank to check
     */
    fun isHighestRank(rank: Rank) =
        highestRank?.name?.equals(rank.name) == true

    /**
     * Returns {@code true} if the player has a certain rank
     * @param rank The rank
     */
    fun hasRank(rank: Rank): Boolean =
        ranks.contains(rank)

    /**
     * Adds a rank to the player
     * @param rank The rank to add to the player
     */
    fun addRank(rank: Rank): Boolean {
        if (this.hasRank(rank))
            return false

        Ranks.modify(rank.name) {
            playerIDs.add(this@RankPlayer.bukkitPlayer.uniqueId)
        }

        // Trigger event
        RankPlayerAddEvent(rank, this).callEvent()

        return true
    }

    /**
     * Removes a rank to the player
     * @param rank The rank to remove from the player
     */
    fun removeRank(rank: Rank): Boolean {
        if (!this.hasRank(rank))
            return false

        Ranks.modify(rank.name) {
            playerIDs.remove(this@RankPlayer.bukkitPlayer.uniqueId)
        }

        // Trigger event
        RankPlayerRemoveEvent(rank, this).callEvent()

        return true
    }
}