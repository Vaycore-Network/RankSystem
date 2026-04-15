package de.c4vxl.ranksystem.data

import de.c4vxl.ranksystem.event.data.RankDataChangeEvent

/**
 * Cache object
 */
data class RankCache(
    val data: Rank,
    var isDirty: Boolean = false
) {
    /**
     * Marks the cache as dirty
     */
    fun makeDirty() {
        isDirty = true
        RankDataChangeEvent(this.data).callEvent()
    }
}
