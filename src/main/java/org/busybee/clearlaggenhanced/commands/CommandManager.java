package org.busybee.clearlaggenhanced.commands;

import co.aikar.commands.PaperCommandManager;
import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.commands.impl.ConfigCommands;
import org.busybee.clearlaggenhanced.commands.impl.DiagnosticCommands;
import org.busybee.clearlaggenhanced.commands.impl.MainCommands;
import org.busybee.clearlaggenhanced.commands.impl.ModuleCommands;
import org.busybee.clearlaggenhanced.utils.Logger;

/**
 * Manages all plugin commands using ACF (Aikar's Command Framework)
 */
public class CommandManager {
    
    private final ClearLaggEnhanced plugin;
    private PaperCommandManager commandManager;
    
    public CommandManager(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
    }
    
    public void registerAll() {
        Logger.info("Registering commands...");
        
        // Initialize ACF command manager
        commandManager = new PaperCommandManager(plugin);

        // Register a dynamic completion for @modules
        commandManager.getCommandCompletions().registerAsyncCompletion("@modules", c ->
            new java.util.ArrayList<>(plugin.getModuleManager().getAllModuleNames())
        );
        
        // Register command classes
        commandManager.registerCommand(new MainCommands(plugin));
        commandManager.registerCommand(new DiagnosticCommands(plugin));
        commandManager.registerCommand(new ModuleCommands(plugin));
        commandManager.registerCommand(new ConfigCommands(plugin));
        
        Logger.info("Commands registered successfully");
    }
    
    public void shutdown() {
        if (commandManager != null) {
            commandManager.unregisterCommands();
        }
    }
    
    public PaperCommandManager getAcfManager() {
        return commandManager;
    }
}