# ClearLaggEnhanced

The ultimate performance management plugin for Minecraft servers (1.8 - 1.21+). A complete rewrite of the classic ClearLag plugin with modern features and intelligent optimization.

## 🚀 Features

### Core Philosophy
- **Legacy Honored, Technology Advanced**: Every feature from the original ClearLag, but rebuilt for modern servers
- **Asynchronous & Thread-Safe**: All heavy operations run off the main thread
- **Intelligence over Brute Force**: Smart decisions based on real-time data
- **Ultimate Compatibility**: Built for Paper API with graceful Spigot fallback

### Key Modules

#### 🎯 Intelligent Entity Management
- **Density-Based Culling**: Removes entities from lag hotspots, not globally
- **AI Pathfinding Throttling**: Reduces mob CPU usage without killing them
- **Safe Entity Protection**: Never touches important entities (players, villagers, item frames)

#### 🗺️ Proactive Chunk Management
- **Automatic Chunk GC**: Background unloading of inactive chunks
- **Spawn Protection**: Keeps important areas loaded
- **Smart Detection**: Unloads chunks only when truly safe

#### ⚡ Redstone Optimization
- **Clock Detection**: Identifies and throttles redstone clocks
- **Update Limiting**: Prevents redstone lag spikes
- **Non-Breaking**: Slows updates instead of breaking circuits

#### 📦 Advanced Hopper Optimization
- **Transfer Throttling**: Prevents hopper overload
- **Chunk Loading Prevention**: Stops hoppers from loading chunks unnecessarily
- **Item Grouping**: Optimizes bulk transfers

#### 🧠 Heuristics Engine
- **The Brain**: Automatically adjusts all optimizations based on live TPS
- **Dynamic Response**: Increases aggressiveness when TPS drops
- **Smart Recovery**: Reduces optimizations when performance improves

#### 📊 Comprehensive Diagnostics
- **Real-time Monitoring**: TPS, MSPT, memory usage
- **Performance Profiling**: Deep server analysis
- **Health Checks**: Automated server status reports

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/cle` | Main command and help | `clearlaggenhanced.admin` |
| `/cle tps` | Show TPS and MSPT | `clearlaggenhanced.tps` |
| `/cle memory` | Memory usage information | `clearlaggenhanced.memory` |
| `/cle check` | Comprehensive health check | `clearlaggenhanced.check` |
| `/cle clear` | Emergency entity clearing | `clearlaggenhanced.clear` |
| `/cle modules` | List all modules | `clearlaggenhanced.admin` |
| `/cle reload` | Reload configuration | `clearlaggenhanced.reload` |

## 🔧 Installation

1. Download the latest release from [Releases](https://github.com/PureGero/ClearLaggEnhanced/releases)
2. Place the JAR file in your `plugins/` folder
3. Restart your server
4. Configure modules in `plugins/ClearLaggEnhanced/modules/`

## ⚙️ Configuration

The plugin uses a modular configuration system:

- `config.yml` - Main plugin settings
- `modules/entity-manager.yml` - Entity management settings
- `modules/chunk-manager.yml` - Chunk optimization settings
- `modules/heuristics-engine.yml` - Auto-optimization brain settings
- And more...

## 🎮 Legacy Compatibility

All original ClearLag commands work with enhanced functionality:

- `/lagg clear` → `/cle clear` (intelligent clearing)
- `/lagg killmobs` → `/cle killmobs` (with advanced filtering)
- `/lagg tps` → `/cle tps` (enhanced display)
- `/lagg memory` → `/cle memory` (detailed analysis)
- `/lagg check` → `/cle check` (comprehensive diagnostics)

## 🔄 Migration from ClearLag

1. Stop your server
2. Remove the old ClearLag plugin
3. Install ClearLaggEnhanced
4. Start your server - configuration will be automatically created
5. No manual migration needed!

## 📈 Performance

ClearLaggEnhanced is designed to be the solution, not part of the problem:

- **Async Operations**: Heavy work happens off the main thread
- **Smart Scheduling**: Operations spread across multiple ticks
- **Minimal Overhead**: Only acts when necessary
- **Thread-Safe**: Built for modern multi-threaded servers

## 🛠️ Development

### Building
```bash
git clone https://github.com/PureGero/ClearLaggEnhanced.git
cd ClearLaggEnhanced
mvn -DskipTests package
```

### Requirements

- Java 17+
- Paper API 1.21+
- Maven 3.9+

## 📊 Metrics

This plugin uses bStats to collect anonymous usage statistics. This helps us understand how the plugin is used and improve it. You can opt out in the configuration.

## 🤝 Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting PRs.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Original ClearLag developers for the inspiration
- Paper team for the excellent server software
- Aikar for the command framework (ACF)
- SpongePowered for the Configurate library

## 📞 Support

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/PureGero/ClearLaggEnhanced/issues)
- 💬 **Discord**: [Join our server](https://discord.gg/your-discord)
- 📖 **Wiki**: [Documentation](https://github.com/PureGero/ClearLaggEnhanced/wiki)

---

**ClearLaggEnhanced** - Because your server deserves better than lag.