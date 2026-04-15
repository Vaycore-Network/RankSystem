package de.c4vxl.ranksystem.data

import de.c4vxl.ranksystem.Main
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

/**
 * Data class representing a rank
 * @param name The name of the rank
 * @param position The position in tab list
 * @param prefix The prefix prepended to the name of players with this rank
 * @param suffix The suffix appended to the name of players with this rank
 * @param playerIDs The ids of the players that have this rank
 * @param permissions The permissions this rank grants
 */
data class Rank(
    val name: String,
    var position: String = "000",
    var prefix: String = "",
    var suffix: String = "",
    var playerIDs: MutableList<UUID> = mutableListOf(),
    var permissions: MutableList<String> = mutableListOf()
) {
    val players: List<OfflinePlayer>
        get() = playerIDs.map { Bukkit.getOfflinePlayer(it) }

    /**
     * Returns the prefix as a component
     */
    fun prefix(): Component? =
        if (Main.config.getBoolean("config.use-prefix") && prefix.isNotBlank())
            MiniMessage.miniMessage().deserialize(
                (Main.config.getString("config.prefix-format") ?: "\$prefix <gray>|</gray>")
                    .replace("\$prefix", prefix)
            )

        else null

    /**
     * Returns the suffix as a component
     */
    fun suffix(): Component? =
        if (Main.config.getBoolean("config.use-suffix") && suffix.isNotBlank())
            MiniMessage.miniMessage().deserialize(
                (Main.config.getString("config.suffix-format") ?: "\$suffix <gray>|</gray>")
                    .replace("\$suffix", suffix)
            )

        else null
}