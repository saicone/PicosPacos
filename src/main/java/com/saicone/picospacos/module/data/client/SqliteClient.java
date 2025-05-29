package com.saicone.picospacos.module.data.client;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.data.PlayerData;
import com.saicone.picospacos.module.data.DataClient;
import com.saicone.picospacos.module.data.DataMethod;
import com.saicone.picospacos.module.settings.BukkitSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SqliteClient implements DataClient {

    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS players (player VARCHAR(255) PRIMARY KEY NOT NULL, saves INT, items TEXT)";
    private static final String SELECT_PLAYER = "SELECT * FROM players WHERE player = ?";
    private static final String INSERT_OR_REPLACE_PLAYER = "INSERT OR REPLACE INTO players (player, saves, items) VALUES (?, ?, ?)";
    private static final String DELETE_PLAYER = "DELETE FROM players WHERE player = ?";

    private Connection con;

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        if (con == null) {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                PicosPacos.logException(2, e, "The database driver doesn't exists");
            }

            File folder = new File(PicosPacos.get().getDataFolder(), "database");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            try {
                con = DriverManager.getConnection("jdbc:sqlite:" + PicosPacos.get().getDataFolder().getAbsolutePath() + "/database/players.db");
            } catch (SQLException e) {
                PicosPacos.logException(1, e, "Cannot create Sqlite connection");
            }
        }
    }

    @Override
    public void onEnable() {
        try (PreparedStatement stmt = con.prepareStatement(CREATE_TABLE)) {
            stmt.execute();
        } catch (SQLException e) {
            PicosPacos.logException(1, e, "Cannot create players table");
        }
    }

    @Override
    public void onDisable() {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @Nullable PlayerData loadPlayerData(@NotNull DataMethod method, @NotNull String name, @NotNull UUID uniqueId) {
        try (PreparedStatement stmt = con.prepareStatement(SELECT_PLAYER)) {
            stmt.setString(1, method == DataMethod.UUID ? uniqueId.toString() : name);

            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                PlayerData data = new PlayerData(name, uniqueId, result.getInt("saves"));
                data.setSaved(true);
                data.addItemsBase64(result.getString("items"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void savePlayerData(@NotNull DataMethod method, @NotNull PlayerData data) {
        if (data.isSaved() && data.getItems().isEmpty() && data.getSaves() < 1) {
            try (PreparedStatement stmt = con.prepareStatement(DELETE_PLAYER)) {
                stmt.setString(1, method == DataMethod.UUID ? data.getUniqueId().toString() : data.getName());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement(INSERT_OR_REPLACE_PLAYER)) {
            stmt.setString(1, method == DataMethod.UUID ? data.getUniqueId().toString() : data.getName());
            stmt.setInt(2, data.getSaves());
            stmt.setString(3, data.getItemsBase64());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}