package com.saicone.picospacos.module.hook;

public interface HookType {

    String pluginName();

    void load();

    default void reload() { }

    void unload();
}
