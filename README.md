# ClearLaggEnhanced

A modern, customizable lag prevention plugin for Minecraft servers. This plugin is designed to help server owners maintain optimal performance by automatically clearing entities, limiting lag-causing mechanics, and providing detailed performance monitoring.

## Special Thanks

Special thanks to **bob7l**, the original developer of ClearLagg, whose work inspired this enhanced version. This plugin builds upon the foundation laid by the original ClearLagg plugin.

## Features

### Core Features
- **Automatic Entity Clearing**: Remove dropped items, experience orbs, and entities at configurable intervals
- **Smart Whitelist System**: Protect only what you want - everything else gets cleared automatically
- **Advanced Lag Prevention**:
  - **Per-Type Mob Limiter** with individual species limits (e.g., max 10 zombies per chunk)
  - **Smart Redstone Limiter** with block-level and chunk-level controls (inspired by RedstoneLimiter)
  - **Hopper Optimization** with transfer cooldown and dynamic throttling
  - **Spawner Control** with delay multipliers
- **Performance Monitoring**: Real-time TPS and memory usage tracking with color-coded indicators
- **Smart Protection**: Protect named entities, tamed animals, and whitelisted entities
- **Customizable Notifications**: Warning system before entity clearing with multiple display options
- **Interactive Admin GUI**: Easy-to-use graphical interface with real-time performance updates
- **PlaceholderAPI Support**: Use plugin data in other plugins
- **Optimized Database**: SQLite with HikariCP pooling and strategic indexes for 10-100x faster queries
- **Folia Compatible**: Full support for multi-threaded servers

### What's New in v2.0-Beta
- 🚀 **Folia Support**: Full compatibility with multi-threaded Folia servers
- 🧠 **Smart Redstone Limiter**: Complete rewrite with block-level and chunk-level controls
  - Time-based reset periods (not per-tick)
  - Individual block activation limits
  - Chunk-wide activation limits
  - Piston push/pull limits
  - Inspired by the popular RedstoneLimiter plugin
- 🐾 **Per-Type Mob Limiter**: Set individual limits for each mob species
  - Example: Max 10 zombies, 15 villagers, 8 creepers per chunk
  - Works alongside global mob limit
- 🎨 **Non-Italic GUI**: All GUI text now displays in regular font (not italic)
- 📝 **Improved Messages**: Better debug output for lag prevention modules

## Installation

1. Download the latest ClearLaggEnhanced.jar file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using the generated config files

## Commands

All commands use the base command `/lagg` with the following aliases: `/clearlagg`, `/clearlag`, `/cl`, `/cle`

| Command        | Description                          | Permission   | Default     |
|----------------|--------------------------------------|--------------|-------------|
| `/lagg help`   | Show help menu                       | `CLE.help`   | All players |
| `/lagg clear`  | Manually clear entities now          | `CLE.clear`  | OP only     |
| `/lagg next`   | Show time until next automatic clear | `CLE.next`   | All players |
| `/lagg tps`    | Display current server TPS           | `CLE.tps`    | OP only     |
| `/lagg ram`    | Show memory usage information        | `CLE.ram`    | OP only     |
| `/lagg admin`  | Open the admin GUI (players only)    | `CLE.admin`  | OP only     |
| `/lagg reload` | Reload plugin configuration          | `CLE.reload` | OP only     |

## Permissions

| Permission   | Description                       | Default  |
|--------------|-----------------------------------|----------|
| `CLE.*`      | All ClearLaggEnhanced permissions | OP       |
| `CLE.help`   | Access to help command            | True     |
| `CLE.clear`  | Access to manual clearing         | OP       |
| `CLE.next`   | Access to next clear timer        | True     |
| `CLE.tps`    | Access to TPS command             | OP       |
| `CLE.ram`    | Access to memory command          | OP       |
| `CLE.admin`  | Access to admin GUI               | OP       |
| `CLE.reload` | Access to reload command          | OP       |

## PlaceholderAPI Placeholders

If PlaceholderAPI is installed, you can use these placeholders in other plugins:

| Placeholder                            | Description              | Example Output  |
|----------------------------------------|--------------------------|-----------------|
| `%clearlagenhanced_tps%`               | Current server TPS       | `19.85`         |
| `%clearlagenhanced_memory_used%`       | Used memory in MB        | `2048`          |
| `%clearlagenhanced_memory_max%`        | Maximum memory in MB     | `4096`          |
| `%clearlagenhanced_memory_percentage%` | Memory usage percentage  | `50.0`          |
| `%clearlagenhanced_entities_total%`    | Total entities on server | `1250`          |
| `%clearlagenhanced_next_clear%`        | Seconds until next clear | `180`           |

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

##### Mob Limiter (Per-Type Limits)
```yaml
lag-prevention:
  mob-limiter:
    enabled: true
    max-mobs-per-chunk: 50  # Global limit

    # Per-type limits (NEW!)
    per-type-limits:
      enabled: true
      limits:
        ZOMBIE: 10
        VILLAGER: 15
        COW: 12
        CREEPER: 8
        # Add any mob type
```

##### Smart Redstone Limiter
```yaml
  redstone-limiter:
    enabled: false
    reset-period-ms: 3000  # Reset counters every 3 seconds

    # Block-level limiting (per individual block)
    blocks:
      enabled: true
      threshold:
        GLOBAL: 2           # Default for all blocks
        PISTON: 1           # Each piston activates 1x per period
        OBSERVER: 3
        DISPENSER: 2

    # Chunk-level limiting (total per chunk)
    chunks:
      enabled: true
      threshold: 1024       # Total activations per chunk

    # Piston push limit
    max-piston-push: 12     # Max blocks pushed/pulled
```

##### Hopper Limiter
```yaml
  hopper-limiter:
    enabled: false
    transfer-cooldown: 8    # ticks between transfers
    max-hoppers-per-chunk: 0  # 0 = disabled
```

##### Spawner Limiter
```yaml
  spawner-limiter:
    enabled: false
    spawn-delay-multiplier: 1.5
```

#### Misc Entity Limiter: Protect named/tagged entities
This limiter trims non-mob entities per chunk (e.g., armor stands, item frames, boats). You can exempt entities by name or by scoreboard tag under:

lag-prevention.misc-entity-limiter.protect

- named: true — Entities with a custom name won’t be trimmed.
- tags — Any entity with one of these tags is protected (example uses CLE_PROTECTED).

Example:
```yml
lag-prevention:
  misc-entity-limiter:
    protect:
      named: true
      tags:
        - "CLE_PROTECTED"
```

Add a tag in-game:
- Command: /tag <selector> add CLE_PROTECTED
  - Example: /tag @e[type=armor_stand,limit=1,sort=nearest] add CLE_PROTECTED

Note: These protections apply only to the misc-entity-limiter (not the global entity-clearing section).

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

- **Minecraft Version**: 1.20 – 1.21.9
- **Java Version**: 17+
- **Optional Dependencies**:
  - PlaceholderAPI (for placeholder support)

## License

This project is licensed under the MIT License. See the LICENSE file for details.

---

*ClearLaggEnhanced - Keeping your server running smoothly*
