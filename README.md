# ClearLag-V2

ClearLag-V2 is a powerful Minecraft server optimization plugin designed to reduce lag and improve server performance. This modernized version builds upon the original ClearLag plugin with enhanced features and compatibility with the latest Minecraft versions.

## Features

- **Entity Management**: Automatically remove unnecessary entities to reduce server load
- **TPS Monitoring**: Monitor server TPS (Ticks Per Second) and take automatic actions when it drops
- **Memory Management**: Track memory usage and execute commands when limits are reached
- **Chunk Control**: Limit entities per chunk to prevent lag hotspots
- **Mob Spawner Limiting**: Control mob spawner rates to prevent server overload
- **Item Lifespan Control**: Customize how long items remain on the ground
- **Performance Optimizations**: Various features to optimize hoppers, TNT, fire spread, and more
- **Customizable Triggers**: Create custom triggers for entity removal based on TPS or entity counts
- **Multi-language Support**: Includes translations for multiple languages

## Requirements

- Java 17 or higher
- Spigot/Paper server (Minecraft 1.13 - 1.21+)

## Installation

1. Download the latest version of ClearLag-V2 from [Modrinth](https://modrinth.com/plugin/clearlag-v2) or [GitHub Releases](https://github.com/yourusername/ClearLag-V2/releases)
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin by editing the `plugins/ClearLag/config.yml` file

## Commands

All commands use the base command `/lagg`:

| Command | Description | Permission |
|---------|-------------|------------|
| `/lagg clear` | Manually clear entities | lagg.clear |
| `/lagg killmobs` | Kill mobs (respects filter) | lagg.killmobs |
| `/lagg reload` | Reload configuration | lagg.reload |
| `/lagg check` | Check entity counts | lagg.check |
| `/lagg tps` | Check server TPS | lagg.tps |
| `/lagg gc` | Run garbage collection | lagg.gc |
| `/lagg unloadchunks` | Unload unused chunks | lagg.unloadchunks |
| `/lagg halt [on/off]` | Halt various server activities | lagg.halt |
| `/lagg profile` | Profile server events | lagg.profile |
| `/lagg samplememory` | Sample memory usage | lagg.samplememory |
| `/lagg sampleticks` | Sample tick rates | lagg.sampleticks |
| `/lagg memory` | Check memory usage | lagg.memory |
| `/lagg performance` | Check server performance | lagg.performance |
| `/lagg area <radius>` | Clear entities in an area | lagg.area |
| `/lagg chunk` | View chunk information | lagg.chunk |
| `/lagg tpchunk <x> <z>` | Teleport to a chunk | lagg.tpchunk |
| `/lagg checkchunk` | Check entities in current chunk | lagg.checkchunk |

## Configuration

ClearLag-V2 is highly configurable. The main configuration file is located at `plugins/ClearLag/config.yml`. Some key configuration sections include:

### Auto-Removal
Configure automatic removal of entities at specified intervals:
```yaml
auto-removal:
  enabled: true
  autoremoval-interval: 460  # seconds
  broadcast-removal: true
  # Configure which entities to remove
```

### TPS Monitoring
Set up automatic actions when server TPS drops:
```yaml
tps-meter:
  enabled: false
  tps-trigger: 14.0  # TPS threshold to trigger actions
  tps-recover: 19.0  # TPS threshold to recover
  # Commands to execute when triggered
```

### Entity Limiting
Limit entities per chunk to prevent lag hotspots:
```yaml
per-entity-chunk-entity-limiter:
  enabled: false
  # Configure limits per entity type
```

For a complete guide to configuration, visit the [Configuration Wiki](https://github.com/yourusername/ClearLag-V2/wiki/Configuration).

## Permissions

ClearLag-V2 uses a permission-based system for command access:

- `lagg.clear` - Permission to clear entities
- `lagg.reload` - Permission to reload configuration
- `lagg.check` - Permission to check entity counts
- `lagg.killmobs` - Permission to kill mobs
- `lagg.unloadchunks` - Permission to unload chunks
- `lagg.help` - Permission to see commands
- `lagg.gc` - Permission to run garbage collection
- `lagg.tps` - Permission to check TPS
- `lagg.samplememory` - Permission to sample memory
- `lagg.sampleticks` - Permission to sample ticks
- `lagg.profile` - Permission to profile events
- `lagg.memory` - Permission to check memory
- `lagg.performance` - Permission to check performance
- `lagg.area` - Permission to clear entities in an area
- `lagg.chunk` - Permission to view chunk information
- `lagg.tpchunk` - Permission to teleport to chunks
- `lagg.checkchunk` - Permission to check entities in current chunk

## Frequently Asked Questions

### How do I filter entities from being removed?
You can configure entity filters in the config.yml file. For example, to prevent named entities from being removed:
```yaml
kill-mobs:
   remove-named: false
```

### How do I customize auto-removal warnings?
Configure warnings in the auto-removal section:
```yaml
warnings:
  - 'time:400 msg:&4[ClearLag] &cWarning Ground items will be removed in &7+remaining &cseconds!'
```

### How do I change the language?
Set your preferred language in the settings section:
```yaml
settings:
  language: English  # Available: English, French, German, Spanish, etc.
```

## Support

If you encounter any issues or have questions:
- Create an issue on [GitHub](https://github.com/yourusername/ClearLag-V2/issues)
- Join our Discord server: [Discord Invite Link]

## License

ClearLag-V2 is licensed under [LICENSE TYPE]. See the LICENSE file for details.

## Credits

- Original author: bob7l
- Current maintainer: [Your Name/Team]
- Contributors: [List of contributors]
- Translators: [List of translators]