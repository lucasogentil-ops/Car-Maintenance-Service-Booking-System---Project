// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestCleanupData {

    public static void main(String[] args) {
        // I used a mode flag here so I can run safe preview or delete actions.
        String mode = args.length > 0 ? args[0].trim().toLowerCase() : "preview";
        boolean confirm = args.length > 1 && "--confirm".equalsIgnoreCase(args[1]);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            printSummary(conn);

            // Each mode does a different cleanup level, so I keep them separate.
            switch (mode) {
                case "preview":
                    System.out.println("Mode: preview (no changes made)");
                    conn.rollback();
                    return;

                case "soft-cancel":
                    requireConfirm(confirm, mode);
                    int softUpdated = softCancelActiveAppointments(conn);
                    conn.commit();
                    System.out.println("Soft cleanup completed. Appointments marked Cancelled: " + softUpdated);
                    break;

                case "delete-cancelled":
                    requireConfirm(confirm, mode);
                    int deletedCancelled = deleteCancelledAndBookedAppointments(conn);
                    conn.commit();
                    System.out.println("Hard cleanup completed. Cancelled + Booked + no-status appointments deleted: " + deletedCancelled);
                    break;

                case "reset-appointments":
                    requireConfirm(confirm, mode);
                    int deletedAll = resetAppointmentsOnly(conn);
                    conn.commit();
                    System.out.println("Reset completed. Appointments deleted: " + deletedAll);
                    break;

                default:
                    conn.rollback();
                    printUsage();
                    return;
            }

            printSummary(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void requireConfirm(boolean confirm, String mode) {
        if (!confirm) {
            throw new IllegalArgumentException(
                "Refusing to run mode '" + mode + "' without --confirm. Example: java TestCleanupData " + mode + " --confirm"
            );
        }
    }

    private static int softCancelActiveAppointments(Connection conn) throws SQLException {
        String sql = "UPDATE appointment SET status = 'Cancelled' WHERE status IS NULL OR status <> 'Cancelled'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    private static int deleteCancelledAndBookedAppointments(Connection conn) throws SQLException {
        // Delete the linked payments first so the appointment rows can be removed cleanly.
        int paymentRows = deletePaymentsForCancelledAndBookedAppointments(conn);
        int apptRows;

        String deleteApptSql = "DELETE FROM appointment WHERE status IN ('Cancelled', 'Booked') OR status IS NULL OR TRIM(status) = ''";
        try (PreparedStatement ps = conn.prepareStatement(deleteApptSql)) {
            apptRows = ps.executeUpdate();
        }

        System.out.println("Related payment rows deleted: " + paymentRows);
        return apptRows;
    }

    private static int deletePaymentsForCancelledAndBookedAppointments(Connection conn) throws SQLException {
        String sql = """
            DELETE FROM payment
            WHERE appointment_id IN (
                SELECT appointment_id FROM appointment
                WHERE status IN ('Cancelled', 'Booked')
                   OR status IS NULL
                   OR TRIM(status) = ''
            )
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    private static int resetAppointmentsOnly(Connection conn) throws SQLException {
        int paymentsDeleted;
        int appointmentsDeleted;

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM payment")) {
            paymentsDeleted = ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM appointment")) {
            appointmentsDeleted = ps.executeUpdate();
        }

        // Reset auto-increment sequence when SQLite tracks it.
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sqlite_sequence WHERE name = 'appointment'")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
            // Ignore if sqlite_sequence table does not exist.
        }

        System.out.println("Payments deleted: " + paymentsDeleted);
        return appointmentsDeleted;
    }

    private static void printSummary(Connection conn) throws SQLException {
        // This gives me a quick check of what is still left in the table.
        String sql = """
            SELECT
                COUNT(*) AS total,
                SUM(CASE WHEN status = 'Cancelled' THEN 1 ELSE 0 END) AS cancelled,
                SUM(CASE WHEN status = 'Booked' THEN 1 ELSE 0 END) AS booked,
                SUM(CASE WHEN status IS NULL OR TRIM(status) = '' THEN 1 ELSE 0 END) AS no_status
            FROM appointment
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int cancelled = rs.getInt("cancelled");
                int booked = rs.getInt("booked");
                int noStatus = rs.getInt("no_status");
                System.out.println("Current summary -> total: " + total + ", booked: " + booked + ", cancelled: " + cancelled + ", no-status: " + noStatus);
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java TestCleanupData preview");
        System.out.println("  java TestCleanupData soft-cancel --confirm");
        System.out.println("  java TestCleanupData delete-cancelled --confirm");
        System.out.println("  java TestCleanupData reset-appointments --confirm");
        System.out.println();
        System.out.println("Modes:");
        System.out.println("  preview            Show summary only; no DB changes.");
        System.out.println("  soft-cancel        Keep records, mark active appointments as Cancelled.");
        System.out.println("  delete-cancelled   Permanently delete cancelled + booked + no-status appointments (+ their payments).");
        System.out.println("  reset-appointments Delete all appointments and all payments.");
    }
}
