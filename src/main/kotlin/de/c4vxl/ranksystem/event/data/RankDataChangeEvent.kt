package de.c4vxl.ranksystem.event.data

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.type.RankEvent

/**
 * Triggered when a ranks data gets modified
 * @param rank The rank that was modified
 */
data class RankDataChangeEvent(
    val rank: Rank
): RankEvent()