package de.c4vxl.papertemplate.ranks

import de.c4vxl.papertemplate.data.Database
import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.update.RankPlayerAddEvent
import de.c4vxl.papertemplate.event.update.RankPlayerRemoveEvent
import org.bukkit.OfflinePlayer

/**
 * Central access point for managing ranks of players
 */
object RankManager {
    /**
     * Returns all ranks of a player
     * @param player The player
     */
    fun getRanks(player: OfflinePlayer): List<Rank> =
        Database.registeredRanks.values
            .filter { it.playerIDs.contains(player.uniqueId) }
            .sortedBy { it.position }

    /**
     * Returns the rank of a player with the highest position
     * @param player The player
     */
    fun getHighestRank(player: OfflinePlayer): Rank? =
        getRanks(player).firstOrNull()

    /**
     * Returns {@code true} if a rank is the players highest one
     * @param player The player
     * @param rank The rank
     */
    fun isHighestRank(player: OfflinePlayer, rank: Rank): Boolean =
        getHighestRank(player)?.name?.equals(rank.name) == true

    /**
     * Returns {@code true} if a player has a certain rank
     * @param player The player
     * @param rank The rank
     */
    fun hasRank(player: OfflinePlayer, rank: Rank): Boolean =
        getRanks(player).contains(rank)

    /**
     * Adds a rank to a player
     * @param player The player
     * @param rank The rank
     * @return Returns {@code true} if the rank was added successfully
     */
    fun addRank(player: OfflinePlayer, rank: Rank): Boolean {
        if (hasRank(player, rank))
            return false

        rank.apply {
            rank.playerIDs.add(player.uniqueId)
        }.save()

        // Trigger event
        RankPlayerAddEvent(rank, player).callEvent()

        return true
    }

    /**
     * Removes a rank from a player
     * @param player The player
     * @param rank The rank
     * @return Returns {@code true} if the rank was removed successfully
     */
    fun removeRank(player: OfflinePlayer, rank: Rank): Boolean {
        if (!hasRank(player, rank))
            return false

        rank.apply {
            rank.playerIDs.remove(player.uniqueId)
        }.save()

        // Trigger event
        RankPlayerRemoveEvent(rank, player).callEvent()

        return true
    }
}