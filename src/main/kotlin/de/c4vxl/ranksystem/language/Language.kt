package de.c4vxl.ranksystem.language

import com.google.gson.Gson
import de.c4vxl.ranksystem.Main
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

/**
 * Lookup table for translations based on keys
 * @param translations A map from key to translation
 * @param name The name of the language
 */
class Language(
    val translations: Map<String, String>,
    val name: String
) {
    companion object {
        /**
         * Loads a language
         * @param name The name of the language
         */
        fun get(name: String): Language? {
            // Load translation file
            val content = Language::class.java.getResourceAsStream("/lang/$name.json")
                ?.bufferedReader()
                ?.readText() ?: return null

            // Load translation
            val translations: Map<String, String> = Gson().fromJson(
                content,
                object : TypeToken<Map<String, String>>() {}.type
            )

            return Language(translations, name)
        }

        /**
         * Returns a list of all available languages
         */
        val availableLanguages: List<String>
            get() =
                (Language::class.java
                    .getResourceAsStream("langs")
                    ?.bufferedReader()
                    ?.readText() ?: "").split("\n")

        /**
         * Returns the path to the language db
         */
        private val langsDB
            get() = File(Main.config.getString("language.db") ?: "languages.yml").also {
                it.parentFile.mkdirs()
                it.createNewFile()
            }

        /**
         * Returns the default language set by the server
         */
        val default: Language
            get() = get(
                Main.config.getString("language.default") ?: "english"
            )!!

        /**
         * Holds the language of a player
         */
        var CommandSender.language: Language
            get() {
                val player = this as? Player ?: return default
                val lang = YamlConfiguration.loadConfiguration(langsDB)
                    .getString(player.uniqueId.toString()) ?: return default

                return get(lang) ?: default
            }
            set(value) {
                val player = this as? Player ?: return

                YamlConfiguration.loadConfiguration(langsDB).apply {
                    this.set(player.uniqueId.toString(), value.name)
                    this.save(langsDB)
                }
            }
    }

    /**
     * Resolves the translation of a key from lookup table
     * @param key The key to the translation
     * @param args Arguments to the translation
     */
    fun get(key: String, vararg args: String): String {
        var value = resolveKey(key)

        // Handle args
        args.forEachIndexed { i, s ->
            value = value.replace("$$i", s)
        }

        return value
    }

    /**
     * Returns a styled component with the translation of a key
     * @param key The key to the translation
     * @param args Arguments to the translation
     */
    fun getCmp(key: String, vararg args: String): Component =
        MiniMessage.miniMessage().deserialize(get(key, *args))

    /**
     * Looks up the translation of a key and resolves nested references
     * @param key The key to lookup
     * @param visited A list of previously visited keys to prevent circular references
     */
    private fun resolveKey(key: String, visited: MutableSet<String> = mutableSetOf()): String {
        // Key already visited
        // This prevents circular references leading to stack overflows
        if (!visited.add(key)) return key

        var value = translations.getOrDefault(key, key)

        // Resolve references
        value = Regex("""\$\{([^}]+)}""").replace(value) {
            resolveKey(it.groupValues[1], visited)
        }

        visited.remove(key)
        return value
    }
}