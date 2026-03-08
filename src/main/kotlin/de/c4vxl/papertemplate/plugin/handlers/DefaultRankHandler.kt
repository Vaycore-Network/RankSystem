package de.c4vxl.papertemplate.plugin.handlers

import de.c4vxl.papertemplate.Main
import de.c4vxl.papertemplate.data.Database
import de.c4vxl.papertemplate.ranks.RankManager
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
        val defaultRank = Database.getRank(
            Database.defaultRank ?: return
        ) ?: return

        // Add player to default rank
        RankManager.addRank(event.player, defaultRank)
    }
}