package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.event.update.RankPermissionUpdateEvent
import de.c4vxl.ranksystem.event.update.RankPlayerAddEvent
import de.c4vxl.ranksystem.event.update.RankPlayerRemoveEvent
import de.c4vxl.ranksystem.ranks.RankManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.PermissionAttachment
import java.util.*

/**
 * Handles the permissions of a rank
 */
class PermissionHandler : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, Main.instance)
    }

    // Holds a map of permission attachments per player
    private val attachments = mutableMapOf<UUID, PermissionAttachment>()

    /**
     * Returns a permission attachment from cache or creates one
     * @param player The player
     */
    private fun getAttachment(player: Player) =
        attachments.getOrPut(player.uniqueId) { player.addAttachment(Main.instance) }

    /**
     * Rebuilds the players permissions
     * @param offlinePlayer The player to rebuild the permissions for
     *
     * This will only work if the player is actually connected
     * @return {@code true} if player was connected and permissions were actually updated
     */
    private fun rebuildPermissions(offlinePlayer: OfflinePlayer): Boolean {
        val player = offlinePlayer.player ?: return false
        val attachment = getAttachment(player)

        // Unset old permissions
        attachment.permissions.keys.forEach {
            attachment.unsetPermission(it)
        }

        // Add new permissions
        RankManager.getRanks(player).forEach { rank ->
            rank.permissions.forEach {
                // If permission is called "_all": give player operator permissions
                if (it == "_all") player.isOp = true

                // Otherwise just add permission normally
                else attachment.setPermission(it, true)
            }
        }

        // Refresh player
        player.recalculatePermissions()
        player.updateCommands()

        return true
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        rebuildPermissions(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        attachments.remove(event.player.uniqueId)?.let {
            event.player.removeAttachment(it)
        }
    }

    @EventHandler
    fun onRankRemove(event: RankPlayerRemoveEvent) {
        rebuildPermissions(event.player)
    }

    @EventHandler
    fun onRankAdd(event: RankPlayerAddEvent) {
        rebuildPermissions(event.player)
    }

    @EventHandler
    fun onPermissionUpdate(event: RankPermissionUpdateEvent) {
        event.rank.players.forEach {
            rebuildPermissions(it)
        }
    }
}