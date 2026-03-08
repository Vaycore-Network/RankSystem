package de.c4vxl.ranksystem.plugin.handlers

import de.c4vxl.ranksystem.Main
import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.event.update.RankPermissionUpdateEvent
import de.c4vxl.ranksystem.event.update.RankPlayerAddEvent
import de.c4vxl.ranksystem.event.update.RankPlayerRemoveEvent
import de.c4vxl.ranksystem.ranks.RankManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import java.util.UUID

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
     * Sets the value of a permission
     * @param player The player
     * @param value The new value of the permissions for that specific player
     * @param permissions The permissions to update
     */
    private fun setPermissions(player: Player, value: Boolean, vararg permissions: String) {
        // Permission is operator permission
        if (permissions.contains("_all"))
            player.isOp = value

        // Set permission
        val attachment = getAttachment(player)
        permissions.forEach { attachment.setPermission(it, value) }

        // Update player
        player.updateCommands()
        player.recalculatePermissions()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        RankManager.getRanks(event.player)
            .forEach { rank ->
                setPermissions(
                    event.player.player ?: return,
                    true,
                    *rank.permissions.toTypedArray()
                )
            }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        attachments.remove(event.player.uniqueId)?.let {
            event.player.removeAttachment(it)
        }
    }

    @EventHandler
    fun onRankRemove(event: RankPlayerRemoveEvent) {
        setPermissions(
            event.player.player ?: return,
            false,
            *event.rank.permissions.toTypedArray()
        )
    }

    @EventHandler
    fun onRankAdd(event: RankPlayerAddEvent) {
        setPermissions(
            event.player.player ?: return,
            true,
            *event.rank.permissions.toTypedArray()
        )
    }

    @EventHandler
    fun onPermissionUpdate(event: RankPermissionUpdateEvent) {
        if (event.value)
            event.rank.players
                .mapNotNull { it.player }
                .forEach { player ->
                    setPermissions(player, true, *event.rank.permissions.toTypedArray())
                }

        else
            event.rank.players
                .mapNotNull { it.player }
                .forEach { player ->
                    setPermissions(player, false, *event.rank.permissions.toTypedArray())
                }
    }
}