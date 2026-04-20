// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

import java.sql.Connection;
import db.DBConnection;

public class TestDB {
    public static void main(String[] args) {

        System.out.println("Working dir: " + System.getProperty("user.dir"));

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✅ SQLite connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
