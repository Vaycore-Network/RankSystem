package de.c4vxl.ranksystem.data

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
    }
}
