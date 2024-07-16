package com.saicone.picospacos.module.data.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.module.data.DataClient;
import com.saicone.picospacos.module.data.DataMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class JsonClient implements DataClient {

    private final Gson gson = new Gson();
    private final File players = new File(new File(PicosPacos.get().getDataFolder(), "database"), "players");

    @Override
    public void onEnable() {
        if (!players.exists()) {
            players.mkdirs();
        }
    }

    @Override
    public @Nullable PlayerData loadPlayerData(@NotNull DataMethod method, @NotNull String name, @NotNull UUID uniqueId) {
        File file = new File(players, (method == DataMethod.UUID ? uniqueId.toString() : name) + ".json");
        if (file.exists() && !file.isDirectory()) {
            try (FileInputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[1024];
                int i;
                while ((i = in.read(b)) > 0) {
                    out.write(b, 0, i);
                }
                JsonObject json = gson.fromJson(out.toString(StandardCharsets.UTF_8), JsonObject.class);
                PlayerData data = new PlayerData(name, uniqueId, json.get("saves").getAsInt());
                data.setSaved(true);
                JsonElement items = json.get("items");
                if (items != null) {
                    data.addItemsBase64(items.getAsString());
                }
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void savePlayerData(@NotNull DataMethod method, @NotNull PlayerData data) {
        File file = new File(players, (method == DataMethod.UUID ? data.getUniqueId().toString() : data.getName()) + ".json");
        file.delete();
        if (data.getItems().isEmpty() && data.getSaves() < 1) return;
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject json = new JsonObject();
        json.addProperty("saves", data.getSaves());
        if (!data.getItems().isEmpty()) {
            json.addProperty("items", data.getItemsBase64());
        }

        try (FileWriter fw = new FileWriter(file); BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(json.toString());
            writer.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}