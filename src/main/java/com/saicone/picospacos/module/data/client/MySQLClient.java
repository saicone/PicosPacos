package com.saicone.picospacos.module.data.client;

import com.saicone.picospacos.PicosPacos;
import com.saicone.picospacos.api.object.PlayerData;
import com.saicone.picospacos.module.data.DataClient;
import com.saicone.picospacos.module.data.DataMethod;
import com.saicone.picospacos.module.settings.BukkitSettings;
import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLClient implements DataClient {

    private static final String DRIVER;
    private static final String URL = "jdbc:mysql://{host}:{port}/{database}{flags}";
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `picospacos_players` (player VARCHAR(255) NOT NULL, saves INT, items MEDIUMTEXT, PRIMARY KEY(player))";
    private static final String SELECT_PLAYER = "SELECT `saves`, `items` FROM `picospacos_players` WHERE player = ?";
    private static final String INSERT_PLAYER = "INSERT INTO `picospacos_players` (player, saves, items) VALUES (?, ?, ?)";
    private static final String UPDATE_PLAYER = "UPDATE `picospacos_players` SET saves = ?, items = ? WHERE player = ?";
    private static final String DELETE_PLAYER = "DELETE FROM `picospacos_players` WHERE player = ?";

    static {
        String driver;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driver = "com.mysql.cj.jdbc.Driver";
        } catch (ClassNotFoundException e) {
            driver = "com.mysql.jdbc.Driver";
        }
        DRIVER = driver;
    }

    private Connection con;

    private String url;
    private String username;
    private String password;

    @Override
    public void onLoad(@NotNull BukkitSettings config) {
        // Check if driver exists
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            PicosPacos.logException(2, e, "The database driver doesn't exists");
        }

        final String host = config.getIgnoreCase("host").asString("localhost");
        final int port = config.getIgnoreCase("port").asInt(3306);
        final String database = config.getRegex("(?i)(db|database)(-?name)?").asString("database");
        final String[] flags = config.getRegex("(?i)flags?|propert(y|ies)").asArray(Types.STRING);

        this.url = URL.replace("{host}", host).replace("{port}", String.valueOf(port)).replace("{database}", database);
        if (flags.length < 1) {
            this.url = url.replace("{flags}", "");
        } else {
            this.url = url.replace("{flags}", "?" + String.join("&", flags));
        }
        this.username = config.getRegex("(?i)user(-?name)?").asString("root");
        this.password = config.getIgnoreCase("password").asString("password");

        try {
            con = DriverManager.getConnection(this.url, this.username, this.password);
            if (!con.isValid(PicosPacos.settings().getInt("Database.Sql.timeout", 1000))) {
                PicosPacos.log(1, "The MySQL connection is not valid");
            }
        } catch (SQLException e) {
            PicosPacos.logException(1, e, "Cannot create MySQL connection");
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
        if (data.isSaved()) {
            if (data.getItems().isEmpty() && data.getSaves() < 1) {
                try (PreparedStatement stmt = con.prepareStatement(DELETE_PLAYER)) {
                    stmt.setString(1, method == DataMethod.UUID ? data.getUniqueId().toString() : data.getName());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                try (PreparedStatement stmt = con.prepareStatement(UPDATE_PLAYER)) {
                    stmt.setInt(1, data.getSaves());
                    stmt.setString(2, data.getItemsBase64());
                    stmt.setString(3, method == DataMethod.UUID ? data.getUniqueId().toString() : data.getName());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else if (!data.getItems().isEmpty() || data.getSaves() > 0) {
            try (PreparedStatement stmt = con.prepareStatement(INSERT_PLAYER)) {
                stmt.setString(1, method == DataMethod.UUID ? data.getUniqueId().toString() : data.getName());
                stmt.setInt(2, data.getSaves());
                stmt.setString(3, data.getItemsBase64());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
