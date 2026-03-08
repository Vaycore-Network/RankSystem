package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.Database
import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.update.RankPlayerAddEvent
import de.c4vxl.ranksystem.event.update.RankPlayerRemoveEvent
import de.c4vxl.ranksystem.event.update.RankPrefixUpdateEvent
import de.c4vxl.ranksystem.ranks.RankManager
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Handles proper displaying of prefixes and suffixes
 */
class DisplayHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    /**
     * Returns the team used for displaying a rank to a player
     * @param player The player to display the rank to
     * @param rank The rank
     */
    private fun getSBTeam(player: Player, rank: Rank) =
        "rank_${rank.position}_${player.uniqueId}".let {
            player.scoreboard.getTeam(it)
                ?: player.scoreboard.registerNewTeam(it)
        }

    /**
     * Handles the displaying of a given rank
     * @param rank The rank
     * @param player The player to handle the displaying for
     */
    private fun handle(rank: Rank, player: Player) {
        val team = getSBTeam(player, rank)

        // Remove player from all other rank teams
        player.scoreboard.teams
            .filter { it.name.startsWith("rank_") }
            .forEach { it.removeEntry(player.name) }

        // Display prefix
        if (Main.config.getBoolean("config.use-prefix"))
            team.prefix(
                MiniMessage.miniMessage().deserialize(
                    (Main.config.getString("config.prefix-format") ?: "\$prefix <gray>|</gray>")
                        .replace("\$prefix", rank.prefix)
                )
            )

        // Display suffix
        if (Main.config.getBoolean("config.use-suffix"))
            team.suffix(
                MiniMessage.miniMessage().deserialize(
                    (Main.config.getString("config.suffix-format") ?: "<gray>|</gray> \$suffix")
                        .replace("\$suffix", rank.suffix)
                )
            )

        team.addEntry(player.name)
    }

    @EventHandler
    fun onPrefixUpdate(event: RankPrefixUpdateEvent) {
        event.rank.players
            .filter { RankManager.isHighestRank(it, event.rank) }
            .mapNotNull { it.player }
            .forEach { handle(event.rank, it) }
    }

    @EventHandler
    fun onSuffixUpdate(event: RankPrefixUpdateEvent) {
        event.rank.players
            .filter { RankManager.isHighestRank(it, event.rank) }
            .mapNotNull { it.player }
            .forEach { handle(event.rank, it) }
    }

    /**
     * Displays a players highest rank
     * @param player The player
     */
    private fun displayHighest(player: OfflinePlayer) {
        handle(
            RankManager.getHighestRank(player) ?: return,
            player.player ?: return
        )
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        displayHighest(event.player)
    }

    @EventHandler
    fun onRemove(event: RankPlayerRemoveEvent) {
        displayHighest(event.player)
    }

    @EventHandler
    fun onAdd(event: RankPlayerAddEvent) {
        displayHighest(event.player)
    }
}