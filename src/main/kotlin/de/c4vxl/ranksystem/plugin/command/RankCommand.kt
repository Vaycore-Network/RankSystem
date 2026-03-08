package de.c4vxl.ranksystem.plugin.command

import de.c4vxl.ranksystem.data.Database
import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.language.Language
import de.c4vxl.ranksystem.language.Language.Companion.language
import de.c4vxl.ranksystem.ranks.RankManager
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Command for managing the rank system from in-game
 */
object RankCommand {
    val command = commandTree("ranks") {
        withPermission("de.c4vxl.ranksystem.perms.ranks")
        withFullDescription(Language.default.get("commands.ranks.desc"))
        withAliases("ranksystem", "rs")
        withUsage("/ranks <option>")

        literalArgument("register") {
            textArgument("name") {
                anyExecutor { sender, args ->
                    val name = args.get("name").toString()

                    // Rank already exists
                    if (Database.rankExists(name)) {
                        sender.sendMessage(sender.language.getCmp("command.ranks.register.failure.already"))
                        return@anyExecutor
                    }

                    // Register rank
                    Database.registerRank(Rank(name))
                    sender.sendMessage(sender.language.getCmp("command.ranks.register.success", name))
                }
            }
        }

        literalArgument("unregister") {
            argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                Database.registeredRanks.keys.toTypedArray()
            })) {
                anyExecutor { sender, args ->
                    val rank = args.get("rank").toString()

                    // Rank doesn't exist
                    if (Database.rankExists(name)) {
                        sender.sendMessage(sender.language.getCmp("command.ranks.unregister.failure.already"))
                        return@anyExecutor
                    }

                    // Unregister
                    Database.unregisterRank(rank)
                    sender.sendMessage(sender.language.getCmp("command.ranks.unregister.success"))
                }
            }
        }

        literalArgument("list") {
            anyExecutor { sender, _ ->
                val registered = Database.registeredRanks

                // No ranks registered
                if (registered.isEmpty()) {
                    sender.sendMessage(sender.language.getCmp("command.ranks.list.msg.empty"))
                    return@anyExecutor
                }

                // Send list
                var component = sender.language.getCmp("command.ranks.list.l1")
                registered.values.sortedBy { it.position }.forEach {
                    component = component
                        .appendNewline()
                        .append(sender.language.getCmp("command.ranks.list.l2", it.name, it.position, it.playerIDs.size.toString()))
                }
                sender.sendMessage(component)
            }
        }

        literalArgument("player") {
            argument(StringArgument("player").replaceSuggestions(ArgumentSuggestions.strings {
                Bukkit.getOfflinePlayers().map { it.name }.toTypedArray()
            })) {
                literalArgument("list") {
                    anyExecutor { sender, args ->
                        val name = args.get("player").toString()
                        val player = Bukkit.getOfflinePlayer(name)
                        val ranks = RankManager.getRanks(player)

                        // No ranks registered
                        if (ranks.isEmpty()) {
                            sender.sendMessage(sender.language.getCmp("command.ranks.player.list.msg.empty", name))
                            return@anyExecutor
                        }

                        // Send list
                        var component = sender.language.getCmp("command.ranks.player.list.l1", name)
                        ranks.sortedBy { it.position }.forEach {
                            component = component
                                .appendNewline()
                                .append(sender.language.getCmp("command.ranks.player.list.l2", it.name, it.position, it.playerIDs.size.toString()))
                        }
                        sender.sendMessage(component)
                    }
                }

                literalArgument("add") {
                    argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                        Database.registeredRanks
                            .filter { !it.value.playerIDs.contains((sender.sender as? Player)?.uniqueId ?: return@filter true) }
                            .keys
                            .toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val name = args.get("player").toString()
                            val player = Bukkit.getOfflinePlayer(name)
                            val rankName = args.get("rank").toString()
                            val rank = Database.getRank(rankName)

                            // Rank doesn't exist
                            if (rank == null) {
                                sender.sendMessage(sender.language.getCmp("command.ranks.player.add.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Player already has the rank
                            if (RankManager.hasRank(player, rank)) {
                                sender.sendMessage(sender.language.getCmp("command.ranks.player.add.failure.already", rankName))
                                return@anyExecutor
                            }

                            // Add rank
                            RankManager.addRank(player, rank)
                            sender.sendMessage(sender.language.getCmp("command.ranks.player.add.success", name, rankName))
                        }
                    }
                }

                literalArgument("remove") {
                    argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings { sender ->
                        Database.registeredRanks
                            .filter { it.value.playerIDs.contains((sender.sender as? Player)?.uniqueId ?: return@filter true) }
                            .keys
                            .toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val name = args.get("player").toString()
                            val player = Bukkit.getOfflinePlayer(name)
                            val rankName = args.get("rank").toString()
                            val rank = Database.getRank(rankName)

                            // Rank doesn't exist
                            if (rank == null) {
                                sender.sendMessage(sender.language.getCmp("command.ranks.player.remove.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Player doesn't have the rank
                            if (!RankManager.hasRank(player, rank)) {
                                sender.sendMessage(sender.language.getCmp("command.ranks.player.remove.failure.not", rankName))
                                return@anyExecutor
                            }

                            // Remove rank
                            RankManager.removeRank(player, rank)
                            sender.sendMessage(sender.language.getCmp("command.ranks.player.remove.success", name, rankName))
                        }
                    }
                }
            }
        }

        literalArgument("permission") {
            argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                Database.registeredRanks.keys.toTypedArray()
            })) {
                literalArgument("add") {
                    textArgument("permission") {
                        includeSuggestions(ArgumentSuggestions.strings { arrayOf("_all") })

                        anyExecutor { sender, args ->
                            val name = args.get("rank").toString()
                            val permission = args.get("permission").toString()

                            val success = Database.rank(name) {
                                this.permissions.add(permission)
                            }

                            if (success)
                                sender.sendMessage(sender.language.getCmp("command.ranks.permission.add.success", name))
                            else
                                sender.sendMessage(sender.language.getCmp("command.ranks.permission.add.failure.invalid_rank", name))
                        }
                    }
                }

                literalArgument("remove") {
                    textArgument("permission") {
                        includeSuggestions(ArgumentSuggestions.strings { arrayOf("_all") })

                        anyExecutor { sender, args ->
                            val name = args.get("rank").toString()
                            val permission = args.get("permission").toString()

                            val success = Database.rank(name) {
                                this.permissions.remove(permission)
                            }

                            if (success)
                                sender.sendMessage(sender.language.getCmp("command.ranks.permission.remove.success", name))
                            else
                                sender.sendMessage(sender.language.getCmp("command.ranks.permission.remove.failure.invalid_rank", name))
                        }
                    }
                }

                literalArgument("list") {
                    anyExecutor { sender, args ->
                        val name = args.get("rank").toString()
                        val permissions = Database.getRank(name)?.permissions

                        // Rank doesn't exit
                        if (permissions == null) {
                            sender.sendMessage(sender.language.getCmp("command.ranks.permission.list.failure.invalid_rank", name))
                            return@anyExecutor
                        }

                        // No ranks registered
                        if (permissions.isEmpty()) {
                            sender.sendMessage(sender.language.getCmp("command.ranks.permission.list.msg.empty", name))
                            return@anyExecutor
                        }

                        // Send list
                        var component = sender.language.getCmp("command.ranks.permission.list.l1", name)
                        permissions.sortedBy { if (it.startsWith("_")) "0$it" else "1$it" }.forEach {
                            component = component
                                .appendNewline()
                                .append(sender.language.getCmp("command.ranks.permission.list.l2", it))
                        }
                        sender.sendMessage(component)
                    }
                }
            }
        }

        literalArgument("position") {
            argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                Database.registeredRanks.keys.toTypedArray()
            })) {
                literalArgument("get") {
                    anyExecutor { sender, args ->
                        val rankName = args.get("rank").toString()
                        val rank = Database.getRank(rankName)

                        if (rank == null) {
                            sender.sendMessage(sender.language.getCmp("command.ranks.position.get.failure.invalid_rank", rankName))
                            return@anyExecutor
                        }

                        // Send
                        sender.sendMessage(sender.language.getCmp("command.ranks.position.get.msg", rankName, rank.position))
                    }
                }

                literalArgument("set") {
                    textArgument("position") {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = Database.getRank(rankName)
                            val position = args.get("position").toString()

                            if (rank == null) {
                                sender.sendMessage(sender.language.getCmp("command.ranks.position.set.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Update
                            rank.update { this.position = position }

                            // Send confirmation
                            sender.sendMessage(sender.language.getCmp("command.ranks.position.set.success", rankName))
                        }
                    }
                }
            }
        }
    }
}