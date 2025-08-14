package org.busybee.clearlaggenhanced.commands;

import co.aikar.commands.PaperCommandManager;
import org.busybee.clearlaggenhanced.ClearLaggEnhanced;
import org.busybee.clearlaggenhanced.commands.impl.ConfigCommands;
import org.busybee.clearlaggenhanced.commands.impl.DiagnosticCommands;
import org.busybee.clearlaggenhanced.commands.impl.MainCommands;
import org.busybee.clearlaggenhanced.commands.impl.ModuleCommands;
import org.busybee.clearlaggenhanced.utils.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

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
        // Attempt to disable ACF timings to avoid deprecated Paper timings warnings
        disableAcfTimings(commandManager);

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

    /**
     * Best-effort disabling of ACF Timings creation.
     * 1) Tries to call any known public methods that disable timings if present in this ACF version.
     * 2) Tries settings object methods if exposed.
     * 3) As fallback, force any boolean fields containing "timing" to false and null out any Timing-like fields.
     * All reflection is safely wrapped; failures are ignored to preserve compatibility.
     */
    private void disableAcfTimings(PaperCommandManager mgr) {
        try {
            // Try common setter names on the manager itself
            for (String methodName : Arrays.asList(
                    "setEnableTimings",
                    "setUsingTimings",
                    "setTimingsEnabled",
                    "setUseTimings",
                    "disableTimings"
            )) {
                tryInvokeBooleanSetter(mgr, methodName, false);
            }

            // Try manager.getSettings().setXxxTimings(false)
            try {
                Method getSettings = mgr.getClass().getMethod("getSettings");
                Object settings = getSettings.invoke(mgr);
                if (settings != null) {
                    for (String methodName : Arrays.asList(
                            "setEnableTimings",
                            "setUsingTimings",
                            "setTimingsEnabled",
                            "setUseTimings",
                            "disableTimings"
                    )) {
                        tryInvokeBooleanSetter(settings, methodName, false);
                    }
                    // Generic: call any boolean setter with name containing "timing"
                    for (Method m : settings.getClass().getMethods()) {
                        if (m.getName().toLowerCase().contains("timing")
                                && m.getParameterCount() == 1
                                && (m.getParameterTypes()[0] == boolean.class || m.getParameterTypes()[0] == Boolean.class)) {
                            try { m.invoke(settings, false); } catch (Throwable ignored) {}
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {
                // no settings exposed; continue
            } catch (Throwable t) {
                // swallow to remain safe
            }

            // As a last resort, iterate fields on manager and superclasses
            Class<?> c = mgr.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    String name = f.getName().toLowerCase();
                    boolean nameHasTiming = name.contains("timing");
                    if (!nameHasTiming) continue;
                    try {
                        f.setAccessible(true);
                        Class<?> type = f.getType();
                        if (type == boolean.class || type == Boolean.class) {
                            f.set(mgr, false);
                        } else {
                            // If it looks like a Timing object, null it out to neutralize
                            if (type.getName().toLowerCase().contains("timing")) {
                                f.set(mgr, null);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
                c = c.getSuperclass();
            }

            Logger.debug("ACF command timings disabled (best-effort).");
        } catch (Throwable t) {
            // Do not log as error; just debug to avoid noise if ACF changes
            Logger.debug("ACF timings disable attempt skipped: " + t.getClass().getSimpleName());
        }
    }

    private void tryInvokeBooleanSetter(Object target, String methodName, boolean value) {
        try {
            Method m = target.getClass().getMethod(methodName, boolean.class);
            m.invoke(target, value);
        } catch (NoSuchMethodException e1) {
            try {
                Method m2 = target.getClass().getMethod(methodName, Boolean.class);
                m2.invoke(target, value);
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
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