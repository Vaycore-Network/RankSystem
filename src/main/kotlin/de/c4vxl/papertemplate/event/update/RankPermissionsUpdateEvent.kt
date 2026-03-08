package de.c4vxl.papertemplate.event.update

import de.c4vxl.papertemplate.data.Rank
import de.c4vxl.papertemplate.event.type.RankEvent

/**
 * Gets triggered whenever a ranks permission get updated
 * @param rank The rank
 * @param permission The permission that was updated
 * @param value The new value
 */
data class RankPermissionsUpdateEvent(val rank: Rank, val permission: String, val value: Boolean): RankEvent()