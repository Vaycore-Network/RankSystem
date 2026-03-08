package de.c4vxl.ranksystem.event.update

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.type.RankEvent
import org.bukkit.OfflinePlayer

/**
 * Gets triggered whenever a player gets removed from a rank
 * @param rank The rank
 * @param player The player that was removed
 */
data class RankPlayerRemoveEvent(val rank: Rank, val player: OfflinePlayer): RankEvent()