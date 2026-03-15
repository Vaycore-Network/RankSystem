package de.c4vxl.ranksystem.plugin.command

import de.c4vxl.ranksystem.data.Rank
import de.c4vxl.ranksystem.data.RankDB
import de.c4vxl.ranksystem.language.Language
import de.c4vxl.ranksystem.player.RankPlayer.Companion.rankPlayer
import de.c4vxl.ranksystem.ranks.RankManager
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Command for managing the rank system from in-game
 */
object RankCommand {
    fun language(sender: CommandSender) =
        (sender as? Player)?.rankPlayer?.language ?: Language.default
    
    val command = commandTree("ranks") {
        withPermission("de.c4vxl.ranksystem.perms.ranks")
        withFullDescription(Language.default.get("commands.ranks.desc"))
        withAliases("ranksystem", "rs")
        withUsage("/ranks <option>")

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
                            sender.sendMessage(language(sender).getCmp("command.ranks.player.list.msg.empty", name))
                            return@anyExecutor
                        }

                        // Send list
                        var component = language(sender).getCmp("command.ranks.player.list.l1", name, ranks.size.toString())
                        ranks.sortedBy { it.position }.forEach {
                            component = component
                                .appendNewline()
                                .append(language(sender).getCmp("command.ranks.player.list.l2", it.name, it.position, it.playerIDs.size.toString()))
                        }
                        sender.sendMessage(component)
                    }
                }

                literalArgument("add") {
                    argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings { args ->
                        val uuid = Bukkit.getOfflinePlayer(
                            args.previousArgs.get("player")?.toString()
                                ?: return@strings arrayOf()
                        ).uniqueId

                        RankDB.registeredRanks
                            .filter { !it.value.playerIDs.contains(uuid) }
                            .keys
                            .toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val name = args.get("player").toString()
                            val player = Bukkit.getOfflinePlayer(name)
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            // Rank doesn't exist
                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.player.add.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Player already has the rank
                            if (RankManager.hasRank(player, rank)) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.player.add.failure.already", rankName))
                                return@anyExecutor
                            }

                            // Add rank
                            RankManager.addRank(player, rank)
                            sender.sendMessage(language(sender).getCmp("command.ranks.player.add.success", name, rankName))
                        }
                    }
                }

                literalArgument("remove") {
                    argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings { args ->
                        val uuid = Bukkit.getOfflinePlayer(
                            args.previousArgs.get("player")?.toString()
                                ?: return@strings arrayOf()
                        ).uniqueId

                        RankDB.registeredRanks
                            .filter { it.value.playerIDs.contains(uuid) }
                            .keys
                            .toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val name = args.get("player").toString()
                            val player = Bukkit.getOfflinePlayer(name)
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            // Rank doesn't exist
                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.player.remove.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Player doesn't have the rank
                            if (!RankManager.hasRank(player, rank)) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.player.remove.failure.not", rankName))
                                return@anyExecutor
                            }

                            // Remove rank
                            RankManager.removeRank(player, rank)
                            sender.sendMessage(language(sender).getCmp("command.ranks.player.remove.success", name, rankName))
                        }
                    }
                }
            }
        }

        literalArgument("rank") {
            literalArgument("register") {
                textArgument("name") {
                    anyExecutor { sender, args ->
                        val name = args.get("name").toString()

                        // Rank already exists
                        if (RankDB.rankExists(name)) {
                            sender.sendMessage(language(sender).getCmp("command.ranks.register.failure.already"))
                            return@anyExecutor
                        }

                        // Register rank
                        RankDB.registerRank(Rank(name))
                        sender.sendMessage(language(sender).getCmp("command.ranks.register.success", name))
                    }
                }
            }

            literalArgument("unregister") {
                argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                    RankDB.registeredRanks.keys.toTypedArray()
                })) {
                    anyExecutor { sender, args ->
                        val rank = args.get("rank").toString()

                        // Rank doesn't exist
                        if (RankDB.rankExists(name)) {
                            sender.sendMessage(language(sender).getCmp("command.ranks.unregister.failure.already"))
                            return@anyExecutor
                        }

                        // Unregister
                        RankDB.unregisterRank(rank)
                        sender.sendMessage(language(sender).getCmp("command.ranks.unregister.success", rank))
                    }
                }
            }

            literalArgument("list") {
                anyExecutor { sender, _ ->
                    val registered = RankDB.registeredRanks

                    // No ranks registered
                    if (registered.isEmpty()) {
                        sender.sendMessage(language(sender).getCmp("command.ranks.list.msg.empty"))
                        return@anyExecutor
                    }

                    // Send list
                    var component = language(sender).getCmp("command.ranks.list.l1", registered.size.toString())
                    registered.values.sortedBy { it.position }.forEach {
                        component = component
                            .appendNewline()
                            .append(language(sender).getCmp("command.ranks.list.l2", it.name, it.position, it.playerIDs.size.toString()))
                    }
                    sender.sendMessage(component)
                }
            }

            literalArgument("permission") {
                argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                    RankDB.registeredRanks.keys.toTypedArray()
                })) {
                    literalArgument("add") {
                        textArgument("permission") {
                            includeSuggestions(ArgumentSuggestions.strings { arrayOf("_all") })

                            anyExecutor { sender, args ->
                                val name = args.get("rank").toString()
                                val permission = args.get("permission").toString()

                                val success = RankDB.rank(name) {
                                    this.permissions.add(permission)
                                }

                                if (success)
                                    sender.sendMessage(language(sender).getCmp("command.ranks.permission.add.success", name))
                                else
                                    sender.sendMessage(language(sender).getCmp("command.ranks.permission.add.failure.invalid_rank", name))
                            }
                        }
                    }

                    literalArgument("remove") {
                        textArgument("permission") {
                            includeSuggestions(ArgumentSuggestions.strings {
                                buildList {
                                    add("_all")
                                    RankDB.getRank(it.previousArgs.get("rank").toString())?.permissions?.let {
                                        addAll(it)
                                    }
                                }.toTypedArray()
                            })

                            anyExecutor { sender, args ->
                                val name = args.get("rank").toString()
                                val permission = args.get("permission").toString()

                                val success = RankDB.rank(name) {
                                    this.permissions.remove(permission)
                                }

                                if (success)
                                    sender.sendMessage(language(sender).getCmp("command.ranks.permission.remove.success", name))
                                else
                                    sender.sendMessage(language(sender).getCmp("command.ranks.permission.remove.failure.invalid_rank", name))
                            }
                        }
                    }

                    literalArgument("list") {
                        anyExecutor { sender, args ->
                            val name = args.get("rank").toString()
                            val permissions = RankDB.getRank(name)?.permissions

                            // Rank doesn't exit
                            if (permissions == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.permission.list.failure.invalid_rank", name))
                                return@anyExecutor
                            }

                            // No ranks registered
                            if (permissions.isEmpty()) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.permission.list.msg.empty", name))
                                return@anyExecutor
                            }

                            // Send list
                            var component = language(sender).getCmp("command.ranks.permission.list.l1", name)
                            permissions.sortedBy { if (it.startsWith("_")) "0$it" else "1$it" }.forEach {
                                component = component
                                    .appendNewline()
                                    .append(language(sender).getCmp("command.ranks.permission.list.l2", it))
                            }
                            sender.sendMessage(component)
                        }
                    }
                }
            }

            literalArgument("position") {
                argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                    RankDB.registeredRanks.keys.toTypedArray()
                })) {
                    literalArgument("get") {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.position.get.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Send
                            sender.sendMessage(language(sender).getCmp("command.ranks.position.get.msg", rankName, rank.position))
                        }
                    }

                    literalArgument("set") {
                        textArgument("position") {
                            anyExecutor { sender, args ->
                                val rankName = args.get("rank").toString()
                                val rank = RankDB.getRank(rankName)
                                val position = args.get("position").toString()

                                if (rank == null) {
                                    sender.sendMessage(language(sender).getCmp("command.ranks.position.set.failure.invalid_rank", rankName))
                                    return@anyExecutor
                                }

                                // Update
                                rank.update { this.position = position }

                                // Send confirmation
                                sender.sendMessage(language(sender).getCmp("command.ranks.position.set.success", rankName))
                            }
                        }
                    }
                }
            }

            literalArgument("prefix") {
                argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                    RankDB.registeredRanks.keys.toTypedArray()
                })) {
                    literalArgument("get") {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.prefix.get.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            if (rank.prefix.isBlank()) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.prefix.get.failure.blank", rankName))
                                return@anyExecutor
                            }

                            // Send
                            sender.sendMessage(language(sender).getCmp("command.ranks.prefix.get.msg", rankName, rank.prefix))
                        }
                    }

                    literalArgument("set") {
                        greedyStringArgument("prefix") {
                            anyExecutor { sender, args ->
                                val rankName = args.get("rank").toString()
                                val rank = RankDB.getRank(rankName)
                                val prefix = args.get("prefix").toString()

                                if (rank == null) {
                                    sender.sendMessage(language(sender).getCmp("command.ranks.prefix.set.failure.invalid_rank", rankName))
                                    return@anyExecutor
                                }

                                // Update
                                rank.update { this.prefix = prefix }

                                // Send confirmation
                                sender.sendMessage(language(sender).getCmp("command.ranks.prefix.set.success", rankName, prefix))
                            }
                        }
                    }

                    literalArgument("remove") {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.prefix.remove.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Update
                            rank.update { this.prefix = "" }

                            // Send confirmation
                            sender.sendMessage(language(sender).getCmp("command.ranks.prefix.remove.success", rankName))
                        }
                    }
                }
            }

            literalArgument("suffix") {
                argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                    RankDB.registeredRanks.keys.toTypedArray()
                })) {
                    literalArgument("get") {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.suffix.get.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            if (rank.suffix.isBlank()) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.suffix.get.failure.blank", rankName))
                                return@anyExecutor
                            }

                            // Send
                            sender.sendMessage(language(sender).getCmp("command.ranks.suffix.get.msg", rankName, rank.suffix))
                        }
                    }

                    literalArgument("set") {
                        greedyStringArgument("suffix") {
                            anyExecutor { sender, args ->
                                val rankName = args.get("rank").toString()
                                val rank = RankDB.getRank(rankName)
                                val suffix = args.get("suffix").toString()

                                if (rank == null) {
                                    sender.sendMessage(language(sender).getCmp("command.ranks.suffix.set.failure.invalid_rank", rankName))
                                    return@anyExecutor
                                }

                                // Update
                                rank.update { this.suffix = suffix }

                                // Send confirmation
                                sender.sendMessage(language(sender).getCmp("command.ranks.suffix.set.success", rankName, suffix))
                            }
                        }
                    }

                    literalArgument("remove") {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.suffix.remove.failure.invalid_rank", rankName))
                                return@anyExecutor
                            }

                            // Update
                            rank.update { this.suffix = "" }

                            // Send confirmation
                            sender.sendMessage(language(sender).getCmp("command.ranks.suffix.remove.success", rankName))
                        }
                    }
                }
            }

            literalArgument("default") {
                literalArgument("get") {
                    anyExecutor { sender, _ ->
                        val rank = RankDB.defaultRank?.let { RankDB.getRank(it) }

                        if (rank == null) {
                            sender.sendMessage(language(sender).getCmp("command.ranks.default.get.failure.none"))
                            return@anyExecutor
                        }

                        // Send
                        sender.sendMessage(language(sender).getCmp("command.ranks.default.get.msg", rank.name))
                    }
                }

                literalArgument("set") {
                    argument(StringArgument("rank").replaceSuggestions(ArgumentSuggestions.strings {
                        RankDB.registeredRanks.keys.toTypedArray()
                    })) {
                        anyExecutor { sender, args ->
                            val rankName = args.get("rank").toString()
                            val rank = RankDB.getRank(rankName)

                            if (rank == null) {
                                sender.sendMessage(language(sender).getCmp("command.ranks.default.set.failure.invalid_rank"))
                                return@anyExecutor
                            }

                            // Set
                            RankDB.defaultRank = rank.name
                            sender.sendMessage(language(sender).getCmp("command.ranks.default.set.success", rank.name))
                        }
                    }
                }
            }
        }

        literalArgument("language") {
            argument(StringArgument("lang").replaceSuggestions(ArgumentSuggestions.strings {
                Language.availableLanguages.toTypedArray()
            })) {
                playerExecutor { player, args ->
                    val lang = args.get("lang").toString()
                    val language = Language.get(lang)

                    if (language == null) {
                        player.sendMessage(language(player).getCmp("command.ranks.language.failure.invalid", lang))
                        return@playerExecutor
                    }

                    // Set
                    player.rankPlayer.language = language
                    player.sendMessage(language(player).getCmp("command.ranks.language.success", lang))
                }
            }
        }
    }
}