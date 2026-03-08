package de.c4vxl.papertemplate.event.update

import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.type.RankEvent

/**
 * Gets triggered whenever a ranks position gets updated
 * @param rank The rank
 * @param oldPosition The old position
 * @param newPosition The new position
 */
data class RankPositionUpdateEvent(val rank: Rank, val oldPosition: String, val newPosition: String): RankEvent()