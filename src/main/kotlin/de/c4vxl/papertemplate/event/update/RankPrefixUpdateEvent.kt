package de.c4vxl.papertemplate.event.update

import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.type.RankEvent

/**
 * Gets triggered whenever a ranks prefix gets updated
 * @param rank The rank
 * @param oldPrefix The old prefix
 * @param newPrefix The new prefix
 */
data class RankPrefixUpdateEvent(val rank: Rank, val oldPrefix: String, val newPrefix: String): RankEvent()