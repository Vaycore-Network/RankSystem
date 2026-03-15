package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.RankDB
import de.c4vxl.ranksystem.ranks.RankManager
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
        if (RankManager.getRanks(event.player).isNotEmpty())
            return

        // Get default rank
        val defaultRank = RankDB.getRank(
            RankDB.defaultRank ?: return
        ) ?: return

        // Add player to default rank
        RankManager.addRank(event.player, defaultRank)
    }
}