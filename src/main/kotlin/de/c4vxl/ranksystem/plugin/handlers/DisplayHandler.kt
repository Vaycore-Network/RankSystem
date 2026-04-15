package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.Rank
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Scoreboard

/**
 * Handles proper displaying of prefixes and suffixes
 */
class DisplayHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    private fun getSBTeam(position: String, rank: String, scoreboard: Scoreboard = Bukkit.getScoreboardManager().mainScoreboard) =
        /**
         * Returns the team used for displaying a rank to a player
         * @param position The position of the rank
         * @param rank The name of the rank
         * @param scoreboard The scoreboard
         */
        "rank_${position}_${rank}".let {
            scoreboard.getTeam(it)
                ?: scoreboard.registerNewTeam(it)
        }

    /**
     * Handles the displaying of a given rank
     * @param rank The rank
     * @param player The player to handle the displaying for
     */
    private fun handle(rank: Rank, player: Player) {
        val team = getSBTeam(rank.position, rank.name)

        // Remove player from all other rank teams
        player.scoreboard.teams
            .filter { it.name.startsWith("rank_") }
            .forEach { it.removePlayer(player) }

        // Display prefix
        if (Main.config.getBoolean("config.use-prefix")) {
            if (rank.prefix == "")
                team.prefix(Component.empty())
            else
                team.prefix(
                    MiniMessage.miniMessage().deserialize(
                        (Main.config.getString("config.prefix-format") ?: "\$prefix <gray>|</gray>")
                            .replace("\$prefix", rank.prefix)
                    )
                )
        }

        // Display suffix
        if (Main.config.getBoolean("config.use-suffix")) {
            if (rank.suffix == "")
                team.suffix(Component.empty())
            else
                team.suffix(
                    MiniMessage.miniMessage().deserialize(
                        (Main.config.getString("config.suffix-format") ?: "<gray>|</gray> \$suffix")
                            .replace("\$suffix", rank.suffix)
                    )
                )
        }

        team.addPlayer(player)
    }

    // TODO: implement
}