package me.rubenicos.mc.picospacos.api;

import me.rubenicos.mc.picospacos.core.data.Database;

public class PicosPacosAPI {

    public static void registerDataType(String name, Class<? extends Database> type) {
        Database.Instance.addType(name.toUpperCase(), type);
    }

    public static void unregisterDataType(String name) {
        Database.Instance.removeType(name.toUpperCase());
    }
}
