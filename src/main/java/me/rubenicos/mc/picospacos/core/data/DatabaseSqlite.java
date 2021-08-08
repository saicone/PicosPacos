package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.module.Locale;

import java.io.File;
import java.sql.*;

public class DatabaseSqlite extends Database {

    private Connection con;

    @Override
    boolean init() {
        if (con == null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                Locale.log(2, "");
            }

            File folder = new File(PicosPacos.get().getDataFolder() + "/database");
            if (!folder.exists()) folder.mkdirs();

            try {
                con = DriverManager.getConnection("jdbc:sqlite:" + PicosPacos.get().getDataFolder().getAbsolutePath() + "/database/players.db");
            } catch (SQLException e) {
                Locale.log(1, "");
                return false;
            }
        }
        return true;
    }

    @Override
    void enable() {
        super.enable();
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS players (player VARCHAR(255) PRIMARY KEY NOT NULL, saves INT, items TEXT)");
        } catch (SQLException e) {
            Locale.log(1, "");
        }
    }

    @Override
    void disable() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    void save(PlayerData data) {
        if (data.isOnDatabase() && data.getItems().isEmpty() && data.getSaves() < 1) {
            try (Statement stmt = con.createStatement()) {
                stmt.executeUpdate(Locale.replaceArgs("DELETE FROM players WHERE player = {0};", useID ? data.getUuid() : data.getName()));
            } catch (SQLException e) {
                Locale.log(1, "");
            }
            return;
        }

        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(Locale.replaceArgs("INSERT OR REPLACE INTO players (player, saves, items) VALUES ({0}, {1}, {2});", useID ? data.getUuid() : data.getName(), String.valueOf(data.getSaves()), data.items()));
        } catch (SQLException e) {
            Locale.log(1, "");
        }
    }

    @Override
    PlayerData get(String name, String uuid) {
        try (Statement stmt = con.createStatement()) {
            ResultSet result = stmt.executeQuery(Locale.replaceArgs("SELECT * FROM players WHERE player = {0};", useID ? uuid : name));
            if (result.next()) {
                PlayerData data = new PlayerData(name, uuid, result.getInt("saves"));
                data.setOnDB(true);
                data.addItemsBase64(result.getString("items"));
                return data;
            }
        } catch (SQLException e) {
            Locale.log(1, "");
        }
        return null;
    }
}