package me.rubenicos.mc.picospacos.api;

import me.rubenicos.mc.picospacos.core.data.Database;

public class PicosPacosAPI {

    public static void registerDataType(String name, Class<? extends Database> type) {
        Database.getTypes().put(name.toUpperCase(), type);
    }

    public static void unregisterDataType(String name) {
        Database.getTypes().remove(name.toUpperCase());
    }
}
