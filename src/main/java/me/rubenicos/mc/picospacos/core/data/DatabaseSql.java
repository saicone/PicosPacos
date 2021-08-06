package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.object.PlayerData;
import me.rubenicos.mc.picospacos.module.Locale;

import java.sql.*;

public class DatabaseSql extends Database {

    private Connection con;

    private String URL;
    private String USER;
    private String PASS;

    private String INSERT;
    private String DELETE;
    private String UPDATE;
    private String SELECT;

    @Override
    boolean init() {
        // Check if driver exists
        try {
            Class.forName(PicosPacos.getSettings().getString("Database.Sql.class", "com.mysql.jdbc.Driver"));
        } catch (ClassNotFoundException e) {
            Locale.log(2, "");
        }

        // Test database connection
        String url = PicosPacos.getSettings().getString("Database.Sql.url");
        String user = PicosPacos.getSettings().getString("Database.Sql.user");
        String pass = PicosPacos.getSettings().getString("Database.Sql.password");
        if (!url.equals(URL) && !user.equals(USER) && !pass.equals(PASS)) {
            URL = url;
            USER = user;
            PASS = pass;
            try {
                con = DriverManager.getConnection(URL, USER, PASS);
                if (!con.isValid(PicosPacos.getSettings().getInt("Database.Sql.timeout", 1000))) {
                    Locale.log(1, "");
                    return false;
                }
            } catch (SQLException e) {
                Locale.log(1, "");
                return false;
            }
        }

        useID = PicosPacos.getSettings().getString("Database.Method").equalsIgnoreCase("UUID");

        // Update queries
        INSERT = PicosPacos.getSettings().getString("Database.Sql.query.insert", "INSERT INTO picospacos_players (player, saves, items) VALUES (?, ?, ?)");
        DELETE = PicosPacos.getSettings().getString("Database.Sql.query.delete", "DELETE FROM picospacos_players WHERE player = ?");
        UPDATE = PicosPacos.getSettings().getString("Database.Sql.query.update", "UPDATE picospacos_players SET saves = ?, items = ? WHERE player = ?");
        SELECT = PicosPacos.getSettings().getString("Database.Sql.query.select", "SELECT * FROM picospacos_players WHERE player = ?");
        return true;
    }

    @Override
    void enable() {
        super.enable();
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(PicosPacos.getSettings().getString("Database.Sql.query.create", "CREATE TABLE IF NOT EXISTS picospacos_players (player VARCHAR(255) NOT NULL, saves INT, items MEDIUMTEXT, PRIMARY KEY(player))"));
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
        if (data.isOnDatabase() && data.isTrash()) {
            try (PreparedStatement stmt = con.prepareStatement(DELETE)) {
                stmt.setString(1, useID ? data.getUuid() : data.getName());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement(data.onDB() ? UPDATE : INSERT)) {
            stmt.setString(data.onDB() ? 3 : 1, useID ? data.getUuid() : data.getName());
            stmt.setInt(data.onDB() ? 1 : 2, data.getSaves());
            stmt.setString(data.onDB() ? 2 : 3, data.items());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    PlayerData get(String name, String uuid) {
        try (PreparedStatement stmt = con.prepareStatement(SELECT)) {
            stmt.setString(1, useID ? uuid : name);

            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                PlayerData data = new PlayerData(name, uuid, result.getInt("saves"));
                data.setOnDB(true);
                data.addItemsBase64(result.getString("items"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
