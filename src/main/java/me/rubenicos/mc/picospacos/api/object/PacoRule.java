package me.rubenicos.mc.picospacos.api.object;

import me.rubenicos.mc.picospacos.module.Settings;
import org.bukkit.inventory.ItemStack;

public class PacoRule {

    public PacoRule(Settings file, String key) {

    }

    public boolean match(ItemStack item) {
        return true;
    }
}
