package org.busybee.clearlaggenhanced.commands;

import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.commands.impl.ConfigCommands;
import org.busybee.clearlaggenhanced.commands.impl.DiagnosticCommands;
import org.busybee.clearlaggenhanced.commands.impl.MainCommands;
import org.busybee.clearlaggenhanced.commands.impl.ModuleCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ClearLaggCommand implements CommandExecutor {

    private final ClearLaggEnhanced plugin;
    private final ConfigCommands configCommands;
    private final DiagnosticCommands diagnosticCommands;
    private final MainCommands mainCommands;
    private final ModuleCommands moduleCommands;

    public ClearLaggCommand(ClearLaggEnhanced plugin) {
        this.plugin = plugin;
        this.configCommands = new ConfigCommands(plugin);
        this.diagnosticCommands = new DiagnosticCommands(plugin);
        this.mainCommands = new MainCommands(plugin);
        this.moduleCommands = new ModuleCommands(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            this.mainCommands.onHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "help":
                if (!hasPermission(sender, "clearlaggenhanced.help")) return true;
                this.mainCommands.onHelp(sender);
                return true;

            case "tps":
                if (!hasPermission(sender, "clearlaggenhanced.tps")) return true;
                this.diagnosticCommands.onTps(sender);
                return true;

            case "memory":
                if (!hasPermission(sender, "clearlaggenhanced.memory")) return true;
                this.diagnosticCommands.onMemory(sender);
                return true;

            case "check":
                if (!hasPermission(sender, "clearlaggenhanced.check")) return true;
                this.diagnosticCommands.onCheck(sender);
                return true;

            case "reload":
                if (!hasPermission(sender, "clearlaggenhanced.reload")) return true;
                this.mainCommands.onReload(sender);
                return true;

            case "clear":
                if (!hasPermission(sender, "clearlaggenhanced.clear")) return true;
                this.mainCommands.onClear(sender);
                return true;

            case "version":
                if (!hasPermission(sender, "clearlaggenhanced.help")) return true;
                this.mainCommands.onVersion(sender);
                return true;

            case "config":
                if (!hasPermission(sender, "clearlaggenhanced.config")) return true;
                this.configCommands.onConfig(sender);
                return true;

            case "modules":
                if (!hasPermission(sender, "clearlaggenhanced.admin")) return true;
                this.moduleCommands.onModules(sender);
                return true;

            case "module":
                if (!hasPermission(sender, "clearlaggenhanced.admin")) return true;
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + " module <enable|disable> <moduleName>");
                    return true;
                }
                String action = args[1].toLowerCase();
                if (action.equals("enable") || action.equals("disable")) {
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " module " + action + " <moduleName>");
                        return true;
                    }
                    String moduleName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (action.equals("enable")) {
                        this.moduleCommands.onModuleEnable(sender, moduleName);
                    } else {
                        this.moduleCommands.onModuleDisable(sender, moduleName);
                    }
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown module action: " + action);
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " module <enable|disable> <moduleName>");
                    return true;
                }

            default:
                this.mainCommands.onHelp(sender);
                return true;
        }
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (permission == null || permission.isEmpty()) return true;
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }
        return true;
    }
}
