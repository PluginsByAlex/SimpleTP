# SimpleTP Plugin

A simple teleportation plugin for Minecraft servers with offline player support.

## Features

- **Basic Teleportation**: Bring players to you, teleport to other players
- **Offline Player Support**: Set teleportations for offline players that execute when they join
- **Bulk Operations**: Teleport all online players or all players (including offline)
- **Multiverse-Core Integration**: Enhanced cross-world teleportation support
- **Configurable**: Enable/disable features and customize messages
- **Permissions Based**: Full permission system for different command access levels

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/bring <player>` | Teleport a player to you | `SimpleTP.bring` |
| `/bringoff <player>` | Set offline teleport for a player to your location | `SimpleTP.offline` |
| `/bringall` | Teleport all online players to you | `SimpleTP.bringall` |
| `/bringalloff` | Teleport all players (online + offline) to you | `SimpleTP.bringall` + `SimpleTP.offline` |
| `/goto <player>` | Teleport to a player | `SimpleTP.goto` |
| `/gotooff <player>` | Teleport to an offline player's last location | `SimpleTP.offline` |
| `/tpoff <player1> <player2>` | Teleport player1 to player2 (supports offline) | `SimpleTP.offline` |
| `/simpletp [reload]` | Show plugin info or reload configuration | `SimpleTP.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `SimpleTP.bring` | Allows teleporting players to you | op |
| `SimpleTP.bringall` | Allows teleporting all players to you | op |
| `SimpleTP.goto` | Allows teleporting to other players | op |
| `SimpleTP.offline` | Allows offline teleportation features | op |
| `SimpleTP.admin` | Allows administrative commands like reload | op |
| `SimpleTP.*` | Gives access to all SimpleTP commands | op |

## Installation

1. Download the latest SimpleTP.jar from the [releases page](https://github.com/yourusername/SimpleTP/releases)
2. Place the jar file in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin by editing `plugins/SimpleTP/config.yml`
5. Reload with `/simpletp reload` or restart the server

## Configuration

The plugin creates a `config.yml` file with the following options:

```yaml
# Enable or disable offline teleportation features
enable-offline-tp: true

# Enable or disable bring all commands
enable-bringall: true

# Allow console to use commands
enable-console-commands: true

# Enable debug mode for detailed logging
debug-mode: false

# Custom messages (supports color codes with &)
messages:
  player-not-found: "&cPlayer not found!"
  player-offline: "&cPlayer is offline!"
  no-permission: "&cYou don't have permission to use this command!"
  # ... and more
```

## How Offline Teleportation Works

1. **Setting Offline Teleports**: Use commands like `/bringoff <player>` to set a teleport location for an offline player
2. **Player Joins**: When the offline player joins the server, they are automatically teleported to the set location
3. **One-Time Use**: Each offline teleport is used only once and then removed
4. **Last Known Locations**: The plugin tracks where players log out for commands like `/gotooff`

## Multiverse-Core Integration

If Multiverse-Core is installed and enabled:
- Cross-world teleportation is fully supported
- Enhanced world loading capabilities
- Better handling of world-specific teleportations

Without Multiverse-Core:
- Basic cross-world teleportation still works
- Warnings logged for cross-world attempts
- Some edge cases may not be handled as gracefully

## Building from Source

### Requirements
- Java 21 or higher
- Maven 3.6 or higher

### Build Steps
```bash
git clone https://github.com/yourusername/SimpleTP.git
cd SimpleTP
mvn clean package
```

The compiled JAR will be in the `target/` directory.

## API Usage

SimpleTP provides a simple API for other plugins:

```java
SimpleTP plugin = (SimpleTP) Bukkit.getPluginManager().getPlugin("SimpleTP");

// Set a pending teleport for an offline player
plugin.getDataManager().setPendingTeleport(playerUUID, location);

// Get last known location of a player
Location lastLocation = plugin.getDataManager().getLastKnownLocation(playerUUID);

// Check if Multiverse is available
boolean mvEnabled = plugin.getMultiverseHelper().isMultiverseEnabled();
```

## Support

- **Issues**: Report bugs on the [GitHub Issues page](https://github.com/yourusername/SimpleTP/issues)
- **Wiki**: Check the [Wiki](https://github.com/yourusername/SimpleTP/wiki) for detailed documentation
- **Discussions**: Join discussions on the [GitHub Discussions page](https://github.com/yourusername/SimpleTP/discussions)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Changelog

### Version 1.0.0
- Initial release
- Basic teleportation commands
- Offline player support
- Multiverse-Core integration
- Configurable messages and features 