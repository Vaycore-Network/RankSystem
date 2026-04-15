package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.data.Ranks
import de.c4vxl.ranksystem.event.data.RankDataChangeEvent
import de.c4vxl.ranksystem.player.RankPlayer.Companion.rankPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Handles proper displaying of prefixes and suffixes
 */
class DisplayHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    /**
     * Handles the displaying of a specific rank for a specific viewer
     * @param rank The rank to render
     * @param viewer The viewer to render it to
     */
    fun display(rank: Rank, viewer: Player) {
        val scoreboard = viewer.scoreboard
        val team = "${rank.position}_rank_${rank.name}".take(16).let {
            scoreboard.getTeam(it) ?: scoreboard.registerNewTeam(it)
        }

        // Remove entries to prevent clashing
        team.removeEntries(team.entries)

        // Set pre/suffix
        team.prefix(rank.prefix())
        team.suffix(rank.suffix())

        // Get all players that should be displayed under that rank
        val players = rank.players.filter {
            (it.name?.let { n -> scoreboard.getEntryTeam(n) == null } ?: false) // Player not in a team
                    && it.rankPlayer.isHighestRank(rank)                        // Rank is the highest rank of that player
        }

        // Add players
        team.addEntries(players.map { it.name })
    }

    /**
     * Renders all ranks to a player
     * @param viewer The player to render the ranks to
     */
    fun displayAll(viewer: Player) =
        Ranks.registeredRanks.values.forEach { display(it, viewer) }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            displayAll(event.player)
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            displayAll(event.player)
        }
    }

    @EventHandler
    fun onUpdate(event: RankDataChangeEvent) {
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            // Display change to all players
            Bukkit.getOnlinePlayers().forEach { display(event.rank, it) }
        }
    }
}