package de.c4vxl.ranksystem.player

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.language.Language
import de.c4vxl.ranksystem.language.Language.Companion.default
import de.c4vxl.ranksystem.language.Language.Companion.langsDB
import de.c4vxl.ranksystem.ranks.RankManager
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

/**
 * A wrapper around the normal bukkit player
 * @param bukkitPLayer The bukkit player instance
 */
class RankPlayer(
    val bukkitPLayer: Player
) {
    companion object {
        val Player.rankPlayer
            get() = RankPlayer(this)
    }

    /**
     * Holds the language of the player
     */
    var language: Language
        get() {
            val lang = YamlConfiguration.loadConfiguration(Language.langsDB)
                .getString(this.bukkitPLayer.uniqueId.toString()) ?: return default

            return Language.get(lang) ?: default
        }
        set(value) {
            YamlConfiguration.loadConfiguration(langsDB).apply {
                this.set(this@RankPlayer.bukkitPLayer.uniqueId.toString(), value.name)
                this.save(langsDB)
            }
        }

    /**
     * Returns all the ranks this player has
     */
    val ranks: List<Rank>
        get() = RankManager.getRanks(bukkitPLayer)

    /**
     * Returns the highest rank of the player
     */
    val highestRank: Rank?
        get() = RankManager.getHighestRank(bukkitPLayer)
}