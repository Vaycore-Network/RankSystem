package de.c4vxl.papertemplate.event.data

import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.type.RankEvent

/**
 * Gets triggered whenever a new rank is unregistered
 * @param rank The rank that has been unregistered
 */
data class RankUnregisterEvent(val rank: Rank): RankEvent()