
# ClearLag - Modern Lag Reduction Plugin

ClearLag is a powerful and efficient lag reduction plugin for Minecraft servers, now updated to support versions 1.8 through 1.21.5. This plugin helps server administrators manage and reduce lag by providing various tools and automated features to clear entities, manage chunks, and monitor server performance.

## Features

- **Entity Management**: Automatically clear entities at configurable intervals
- **Chunk Management**: Unload unused chunks to free up memory
- **TPS Monitoring**: Monitor server TPS (Ticks Per Second) to identify lag issues
- **Memory Management**: Monitor and manage server memory usage
- **Performance Tools**: Various commands to help diagnose and fix performance issues
- **Customizable**: Extensive configuration options to tailor the plugin to your server's needs

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/lagg clear` | Clear entities | lagg.clear |
| `/lagg reload` | Reload configuration | lagg.reload |
| `/lagg check` | Check entity counts | lagg.check |
| `/lagg killmobs` | Kill all mobs | lagg.killmobs |
| `/lagg unloadchunks` | Unload unused chunks | lagg.unloadchunks |
| `/lagg gc` | Run garbage collection | lagg.gc |
| `/lagg tps` | Check server TPS | lagg.tps |
| `/lagg memory` | Check memory usage | lagg.memory |
| `/lagg performance` | Run performance diagnostics | lagg.performance |

## Permissions

| Permission | Description |
|------------|-------------|
| `lagg.clear` | Permission to clear lag |
| `lagg.reload` | Permission to reload config from disk |
| `lagg.check` | Permission to check entity counts |
| `lagg.killmobs` | Permission to kill mobs |
| `lagg.unloadchunks` | Permission to unload chunks |
| `lagg.help` | Permission to see commands |
| `lagg.gc` | Permission to run garbage collection |
| `lagg.tps` | Permission to see TPS |
| `lagg.memory` | Permission to check memory usage |
| `lagg.performance` | Permission to run performance diagnostics |

## Configuration

The plugin is highly configurable. Check the `config.yml` file for all available options.

## Updates in Version 4.1.0

- Implemented enum-based configuration system for improved type safety and maintainability
- Modernized update checker with GitHub API integration and better error handling
- Enhanced material handling with improved backward compatibility
- Improved async operations with CompletableFuture
- Added better error handling and debugging options
- Enhanced JavaDoc documentation throughout the codebase

## Updates in Version 4.0.0

- Updated to support Minecraft versions 1.8 - 1.21.5
- Removed legacy code for pre-1.8 versions
- Updated to Java 17 for better performance and compatibility
- Modernized codebase for better stability and efficiency
- Updated Maven plugins and dependencies
- Improved error handling and logging

## Installation

1. Download the latest version of ClearLag
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin to your liking by editing the `config.yml` file

## Support

If you encounter any issues or have questions, please open an issue on the GitHub repository.

## License

This project is licensed under the terms of the included LICENSE file.

## History

Clearlag has been around since 2011 and has evolved over the years to keep up with Minecraft's changes. The plugin was originally designed for older versions of Minecraft but has been continuously updated to support the latest versions.

**Previous Modules (Historical)**
- Clearlag Legacy: For Spigot/Bukkit builds before 1.8 (removed in v4.0.0)
- Clearlag Core: For 1.8+ (current module)
