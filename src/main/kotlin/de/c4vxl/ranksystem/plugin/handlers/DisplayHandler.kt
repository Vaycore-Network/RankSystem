package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.data.Ranks
import de.c4vxl.ranksystem.event.data.RankDataChangeEvent
import de.c4vxl.ranksystem.player.RankPlayer.Companion.rankPlayer
import net.kyori.adventure.text.Component
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
        val players = rank.players.filter { player ->
                    // Player name should be cached or player should be online
                    player.name != null

                    // Player already has a team that wants to set a pre-/suffix
                    // Don't overwrite that
                    && scoreboard.getEntryTeam(player.name!!)?.let {
                        it.prefix() == Component.empty() &&
                                it.suffix() == Component.empty()
                    } != false

                    // Rank must be the highest rank for that player
                    && player.rankPlayer.isHighestRank(rank)
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
        Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
            displayAll(event.player)
        }, 10)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        Bukkit.getScheduler().runTaskLater(Main.instance, Runnable {
            displayAll(event.player)
        }, 10)
    }

    @EventHandler
    fun onUpdate(event: RankDataChangeEvent) {
        Bukkit.getScheduler().callSyncMethod(Main.instance) {
            // Display change to all players
            Bukkit.getOnlinePlayers().forEach { display(event.rank, it) }
        }
    }
}