package de.c4vxl.ranksystem.event.data

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.type.RankEvent

/**
 * Gets triggered whenever a new rank is unregistered
 * @param rank The rank that has been unregistered
 */
data class RankUnregisterEvent(val rank: Rank): RankEvent()