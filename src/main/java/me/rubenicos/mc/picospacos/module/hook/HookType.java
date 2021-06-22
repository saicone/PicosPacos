package me.rubenicos.mc.picospacos.module.hook;

public interface HookType {

    String pluginName();

    void load();

    default void reload() { }

    void unload();
}
