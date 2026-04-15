package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.Ranks
import de.c4vxl.ranksystem.player.RankPlayer.Companion.rankPlayer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Handles the default rank
 */
class DefaultRankHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        // Player already has a rank
        if (event.player.rankPlayer.ranks.isNotEmpty())
            return

        // Get default rank
        val defaultRank = Ranks.getDefaultRank() ?: return

        // Add player to default rank
        event.player.rankPlayer.addRank(defaultRank)
    }
}