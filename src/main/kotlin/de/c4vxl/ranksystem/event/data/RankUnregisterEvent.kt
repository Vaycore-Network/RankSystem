package de.c4vxl.ranksystem.event.data

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.type.RankEvent

/**
 * Triggered when a rank gets removed
 * @param rank The rank that was removed
 */
data class RankUnregisterEvent(
    val rank: Rank
): RankEvent()