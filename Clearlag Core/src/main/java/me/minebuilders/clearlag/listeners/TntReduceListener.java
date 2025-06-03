package me.minebuilders.clearlag.listeners;

import me.minebuilders.clearlag.annotations.ConfigPath;
import me.minebuilders.clearlag.annotations.ConfigValue;
import me.minebuilders.clearlag.config.ConfigKey;
import me.minebuilders.clearlag.modules.EventModule;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

@ConfigPath(path = "tnt-reducer")
public class TntReduceListener extends EventModule {

    @ConfigValue(key = ConfigKey.TNT_REDUCER_CHECK_RADIUS)
    private final int checkRadius = 5;

    @ConfigValue(key = ConfigKey.TNT_REDUCER_MAX_PRIMED)
    private final int maxPrimed = 3;

    @EventHandler
    public void onBoom(EntityExplodeEvent event) {

        Entity e = event.getEntity();

        if (e.getType() == EntityType.TNT) {

            int counter = 0;

            for (Entity tnt : e.getNearbyEntities(checkRadius, checkRadius, checkRadius)) {
                if (tnt.getType() == EntityType.TNT) {
                    if (counter > maxPrimed) {
                        tnt.remove();
                    } else {
                        ++counter;
                    }
                }
            }
        }
    }

}
