package me.rubenicos.mc.picospacos.api.object;

import org.bukkit.entity.Player;

public class PlayerData {

    private String name;
    private final String uuid;
    private boolean edited = false;

    public PlayerData(Player player) {
        name = player.getName();
        uuid = player.getUniqueId().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean hasEdited() {
        return edited;
    }
}
