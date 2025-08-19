# ClearLaggEnhanced

A modern, customizable lag prevention plugin for Minecraft servers. This plugin is designed to help server owners maintain optimal performance by automatically clearing entities, limiting lag-causing mechanics, and providing detailed performance monitoring.

## Special Thanks

Special thanks to **bob7l**, the original developer of ClearLagg, whose work inspired this enhanced version. This plugin builds upon the foundation laid by the original ClearLagg plugin.

## Features

- **Automatic Entity Clearing**: Remove dropped items, experience orbs, and other entities at configurable intervals
- **Lag Prevention Modules**: Mob limiter, redstone limiter, hopper optimization, and spawner control
- **Performance Monitoring**: Real-time TPS and memory usage tracking
- **Smart Protection**: Protect named entities, tamed animals, and player-important items
- **Customizable Notifications**: Warning system before entity clearing
- **Admin GUI**: Easy-to-use graphical interface for server management
- **PlaceholderAPI Support**: Use plugin data in other plugins
- **Database Integration**: SQLite database for data persistence

## Installation

1. Download the latest ClearLaggEnhanced.jar file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using the generated config files

## Commands

All commands use the base command `/lagg` with the following aliases: `/clearlagg`, `/cl`, `/cle`

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/lagg help` | Show help menu | `CLE.help` | All players |
| `/lagg clear` | Manually clear entities now | `CLE.clear` | OP only |
| `/lagg next` | Show time until next automatic clear | `CLE.next` | All players |
| `/lagg tps` | Display current server TPS | `CLE.tps` | OP only |
| `/lagg ram` | Show memory usage information | `CLE.ram` | OP only |
| `/lagg admin` | Open the admin GUI (players only) | `CLE.admin` | OP only |
| `/lagg reload` | Reload plugin configuration | `CLE.reload` | OP only |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `CLE.*` | All ClearLaggEnhanced permissions | OP |
| `CLE.help` | Access to help command | True |
| `CLE.clear` | Access to manual clearing | OP |
| `CLE.next` | Access to next clear timer | True |
| `CLE.tps` | Access to TPS command | OP |
| `CLE.ram` | Access to memory command | OP |
| `CLE.admin` | Access to admin GUI | OP |
| `CLE.reload` | Access to reload command | OP |

## PlaceholderAPI Placeholders

If PlaceholderAPI is installed, you can use these placeholders in other plugins:

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%clearlagenhanced_tps%` | Current server TPS | `19.85` |
| `%clearlagenhanced_memory_used%` | Used memory in MB | `2048` |
| `%clearlagenhanced_memory_max%` | Maximum memory in MB | `4096` |
| `%clearlagenhanced_memory_percentage%` | Memory usage percentage | `50.0` |
| `%clearlagenhanced_entities_total%` | Total entities on server | `1250` |
| `%clearlagenhanced_next_clear%` | Seconds until next clear | `180` |

## Configuration

### Main Configuration (`config.yml`)

#### Database Settings
```yaml
database:
  enabled: true
  type: "sqlite"
  file: "data.db"
```

#### Entity Clearing
```yaml
entity-clearing:
  enabled: true
  interval: 300 # seconds (5 minutes)
  protect-named-entities: true
  protect-tamed-entities: true
  
  # Entities that will never be cleared
  whitelist:
    - "PLAYER"
    - "VILLAGER"
    - "ARMOR_STAND"
  
  # Entities that will always be cleared first
  blacklist:
    - "DROPPED_ITEM"
    - "EXPERIENCE_ORB"
    - "ARROW"
  
  # Specific worlds to clear (empty = all worlds)
  worlds: []
```

#### Lag Prevention Modules
```yaml
lag-prevention:
  mob-limiter:
    enabled: true
    max-mobs-per-chunk: 50
    exempt-named-mobs: true
  
  redstone-limiter:
    enabled: true
    max-redstone-per-chunk: 100
    disable-fast-clocks: true
    clock-detection-threshold: 10
  
  hopper-limiter:
    enabled: true
    transfer-cooldown: 8 # ticks
    max-hoppers-per-chunk: 20
  
  spawner-limiter:
    enabled: true
    spawn-delay-multiplier: 1.5
    max-spawners-per-chunk: 10
```

#### Performance Monitoring
```yaml
monitoring:
  enabled: true
  chunk-scan-radius: 5
  entity-threshold: 100
```

#### Notifications
```yaml
notifications:
  enabled: true
  warning-times: [60, 30, 10, 5] # seconds before clearing
  
  methods:
    chat: true
    actionbar: true
    title: false
    sound: true
  
  sound:
    type: "BLOCK_NOTE_BLOCK_PLING"
    volume: 1.0
    pitch: 1.0
```

### Message Configuration (`messages.yml`)

Customize all plugin messages, warnings, and notifications. Supports color codes and placeholders.

```yaml
warnings:
  entity-clear: "<yellow>⚠ Entities will be cleared in <red>{seconds}</red> seconds!"

notifications:
  clear-complete: "<green>✓ Cleared <yellow>{count}</yellow> entities in <gray>{time}ms</gray>!"
```

## Entity Types

### Protected by Default
- Players
- Villagers
- Iron Golems
- Named entities (when `protect-named-entities: true`)
- Tamed animals (when `protect-tamed-entities: true`)
- Armor Stands
- Item Frames
- Paintings

### Commonly Cleared
- Dropped Items
- Experience Orbs
- Arrows and Projectiles
- Hostile Mobs (in excess)
- Primed TNT (configurable)

## Performance Tips

1. **Adjust Clear Interval**: Lower intervals (shorter time) for busier servers
2. **Customize Entity Lists**: Add problematic entities to the blacklist
3. **Enable Lag Prevention**: Use mob limiters and redstone limiters for best results
4. **Monitor Performance**: Use `/lagg tps` and `/lagg ram` regularly
5. **World-Specific Clearing**: Configure different settings for different worlds

## Troubleshooting

### Common Issues

**Entities not being cleared:**
- Check if they're in the whitelist
- Verify they're not named (if protection is enabled)
- Ensure the plugin is enabled in that world

**Performance still poor:**
- Enable more lag prevention modules
- Lower entity limits per chunk
- Consider clearing more frequently

**Commands not working:**
- Check permissions
- Verify the player has the required permission nodes
- Try using full command `/lagg` instead of aliases

### Support

For support, bug reports, or feature requests:
- GitHub Issues: [ClearLaggEnhanced Issues](https://github.com/BusyBee-Development/ClearLaggEnhanced/issues)
- Discord: Contact the development team

## Requirements

- **Minecraft Version**: 1.20+
- **Java Version**: 17+
- **Optional Dependencies**:
  - PlaceholderAPI (for placeholder support)

## License

This project is licensed under the MIT License. See the LICENSE file for details.

---

*ClearLaggEnhanced - Keeping your server running smoothly*