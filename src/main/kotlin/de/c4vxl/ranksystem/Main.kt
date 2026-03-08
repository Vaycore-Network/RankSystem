package de.c4vxl.ranksystem

import de.c4vxl.ranksystem.plugin.command.RankCommand
import de.c4vxl.ranksystem.plugin.handlers.DefaultRankHandler
import de.c4vxl.ranksystem.plugin.handlers.DisplayHandler
import de.c4vxl.ranksystem.plugin.handlers.PermissionHandler
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIPaperConfig
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
        lateinit var logger: Logger
        lateinit var config: FileConfiguration
    }

    override fun onLoad() {
        instance = this
        Main.logger = this.logger

        // Load CommandAPI
        CommandAPI.onLoad(
            CommandAPIPaperConfig(this)
                .silentLogs(true)
                .verboseOutput(false)
        )
    }

    override fun onEnable() {
        // Enable CommandAPI
        CommandAPI.onEnable()

        // Reload config
        saveResource("config.yml", false)
        reloadConfig()
        Main.config = this.config

        // Register handlers
        DefaultRankHandler()
        DisplayHandler()
        PermissionHandler()

        // Register commands
        RankCommand

        logger.info("[+] $name has been enabled!")
    }

    override fun onDisable() {
        // Disable CommandAPI
        CommandAPI.onDisable()

        logger.info("[+] $name has been disabled!")
    }
}