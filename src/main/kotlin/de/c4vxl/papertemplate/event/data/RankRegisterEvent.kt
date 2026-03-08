package de.c4vxl.papertemplate.event.data

import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.type.RankEvent

/**
 * Gets triggered whenever a new rank is registered
 * @param rank The rank that has been registered
 */
data class RankRegisterEvent(val rank: Rank): RankEvent()