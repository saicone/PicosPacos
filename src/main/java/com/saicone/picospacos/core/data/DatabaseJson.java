package com.saicone.picospacos.core.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.object.PlayerData;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DatabaseJson extends Database {

    private final Gson gson = new Gson();
    private final File players = new File(PicosPacos.get().getDataFolder() + "/database/players");

    @Override
    void enable() {
        super.enable();
        if (!players.exists()) {
            players.mkdirs();
        }
    }

    @Override
    void save(PlayerData data) {
        File file = new File(players, (useID ? data.getUuid() : data.getName()) + ".json");
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
            json.addProperty("items", data.items());
        }

        try (FileWriter fw = new FileWriter(file); BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(json.toString());
            writer.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    PlayerData get(String name, String uuid) {
        File file = new File(players, (useID ? uuid : name) + ".json");
        if (file.exists() && !file.isDirectory()) {
            try (FileInputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] b = new byte[1024];
                int i;
                while ((i = in.read(b)) > 0) {
                    out.write(b, 0, i);
                }
                JsonObject json = gson.fromJson(out.toString(StandardCharsets.UTF_8), JsonObject.class);
                PlayerData data = new PlayerData(name, uuid, json.get("saves").getAsInt());
                data.setOnDB(true);
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
}