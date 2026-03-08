package de.c4vxl.ranksystem.event.update

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.type.RankEvent

/**
 * Gets triggered whenever a ranks suffix gets updated
 * @param rank The rank
 * @param oldSuffix The old prefix
 * @param newSuffix The new prefix
 */
data class RankSuffixUpdateEvent(val rank: Rank, val oldSuffix: String, val newSuffix: String): RankEvent()