package de.c4vxl.papertemplate.event.update

import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.type.RankEvent

/**
 * Gets triggered whenever a ranks suffix gets updated
 * @param rank The rank
 * @param oldSuffix The old prefix
 * @param newSuffix The new prefix
 */
data class RankSuffixUpdateEvent(val rank: Rank, val oldSuffix: String, val newSuffix: String): RankEvent()