// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

package db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static final String DB_FILE_NAME = "car_maintenance.db";
    
    public static Connection getConnection() throws SQLException {
        Path dbPath = resolveDatabasePath();
        String sqlitePath = dbPath.toAbsolutePath().normalize().toString().replace('\\', '/');

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQLite JDBC driver not found. Ensure sqlite-jdbc jar is on the runtime classpath.", ex);
        }

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);

        // SQLite does not enforce FK constraints unless explicitly enabled on each connection.
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }

        return conn;

        
    }

    private static Path resolveDatabasePath() throws SQLException {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> candidates = new ArrayList<>();

        // Check a few likely folders because the app can be started from different places.
        candidates.add(cwd.resolve("database").resolve(DB_FILE_NAME));
        candidates.add(cwd.resolve("database").resolve("Car_maintenance.db"));
        candidates.add(cwd.getParent() != null ? cwd.getParent().resolve("database").resolve(DB_FILE_NAME) : null);
        candidates.add(cwd.getParent() != null ? cwd.getParent().resolve("database").resolve("Car_maintenance.db") : null);
        candidates.add(cwd.getParent() != null && cwd.getParent().getParent() != null
                ? cwd.getParent().getParent().resolve("database").resolve(DB_FILE_NAME)
                : null);
        candidates.add(cwd.getParent() != null && cwd.getParent().getParent() != null
                ? cwd.getParent().getParent().resolve("database").resolve("Car_maintenance.db")
                : null);

        for (Path candidate : candidates) {
            if (candidate != null && Files.exists(candidate)) {
                return candidate;
            }
        }

        throw new SQLException("Database file not found. Checked paths: " + candidates);
    }
    
}
