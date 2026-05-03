<br>
<h2 align="center">Rank System</h2>

<p align="center">This plugin provides an advanced rank system to your minecraft network.</p>
<br>
<p align="center">
  <img src="https://img.shields.io/badge/Programming%20Language:-Kotlin-blue" alt="Kotlin Badge" />
  <img src="https://img.shields.io/badge/Type:-Plugin-red" alt="Type Badge" />
  <img src="https://img.shields.io/badge/Topic:-Minecraft-orange" alt="Topic Badge" />
</p>

---

# What is RankSystem?
RankSystem is a Minecraft server plugin that provides a flexible and configurable player ranking system.

It allows server administrators to create ranks, assign them to players, manage permissions, and customize prefixes and suffixes directly from in-game commands.

The plugin is designed for Minecraft networks that require a lightweight but powerful rank management solution.

# Features
- Create and remove ranks dynamically
- Assign multiple ranks to players
- Custom prefix and suffix per rank
- Built-in permission management
- Configurable rank hierarchy
- Default rank support

# Installation
1. Download the latest release.
2. Place the plugin `.jar` file into your server's `plugins/` folder.
3. Start or restart the server.
4. Configure ranks using the `/ranks` command.

# Configuration
```yaml
config:
  # Path to the directory the ranks should be stored in
  db-path: "./ranks/"

  # If set to true pretty printing will be enabled
  db-pretty-print: true

  # Set the default rank players will receive the first time they join
  # default: ""

  # If set to false, no prefixes will be displayed
  use-prefix: true

  # If set to false, no suffixes will be displayed
  use-suffix: true

  # The format used for displaying prefixes
  prefix-format: "$prefix <gray>|</gray> "

  # The format used for displaying suffixes
  suffix-format: " <gray>|</gray> $suffix"

language:
  # Path to the languages database
  db: "./languages.yml"

  # Path to directory to store translation files in to
  translations-dir: "./translations/ranks/"

  # The default language
  default: "en"
```

A project by [c4vxl](https://info.c4vxl.de/)