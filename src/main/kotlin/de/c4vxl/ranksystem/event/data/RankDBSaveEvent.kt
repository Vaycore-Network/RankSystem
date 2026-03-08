package de.c4vxl.ranksystem.event.data

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.type.RankEvent

/**
 * Gets triggered whenever rank gets updated in db
 * @param rank The rank that has been updated
 */
data class RankDBSaveEvent(val rank: Rank): RankEvent()