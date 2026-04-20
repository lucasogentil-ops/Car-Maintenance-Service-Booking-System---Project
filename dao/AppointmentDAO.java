// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

package dao;

import db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public static class LookupOption {
        public final int id;
        public final String label;

        public LookupOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return id + " - " + label;
        }
    }

    // Simple data holder for one appointment row.
    public static class AppointmentRow {
        public int appointmentId;
        public String ownerName;
        public String regNumber;
        public String vehicle;
        public String service;
        public String dateTime;
        public String status;
        public String paymentMethod;
        public String paymentStatus;
        public String notes;
    }

    public static class OwnerItem
    {
        public int id;
        public String name;

        public OwnerItem(int id, String name)
        {
            this.id=id;
            this.name=name;
        }

        @Override
        public String toString()
        {
            return id + " - " + name;
        }
    }

    public static class VehicleItem
    {
        public int id;
        public String description;
        public VehicleItem(int id, String description)
        {
            this.id=id;
            this.description=description;
        }

        @Override
        public String toString()        {
            return id + " - " + description;
        }

    }

    public static class ServiceTypeItem
    {
        public int id;
        public String name;
        public ServiceTypeItem(int id, String name)
        {
            this.id=id;
            this.name=name;
        }

        @Override
        public String toString()        {
            return id + " - " + name;
        }
    }

    public static class paymentType 
    {
        public int id;
        public String method;
        public paymentType(int id, String method) {
            this.id = id;
            this.method = method;
        }
        @Override
        public String toString() {
            return id + " - " + method;
        }
    }

        // READ: Get all appointments
    public List<AppointmentRow> getAllAppointments() throws SQLException 
    {

        String sql = """
            SELECT
              a.appointment_id,
                            COALESCE(o.full_name, '[Unknown Owner]') AS owner_name,
                            COALESCE(v.reg_number, '[No Reg]') AS reg_number,
                            COALESCE(v.make || ' ' || v.model, '[Unknown Vehicle]') AS vehicle,
                            COALESCE(s.name, '[Unknown Service]') AS service,
              a.date_time,
              a.status,
              a.notes,
                            COALESCE(NULLIF(TRIM(p.method), ''), 'Not set') AS payment_method,
                            COALESCE(NULLIF(TRIM(p.payment_status), ''), 'Pending') AS payment_status
            FROM appointment a
                        LEFT JOIN car_owner o ON a.owner_id = o.owner_id
                        LEFT JOIN vehicle v ON a.vehicle_id = v.vehicle_id
                        LEFT JOIN service_type s ON a.service_type_id = s.service_type_id
            LEFT JOIN payment p ON a.appointment_id = p.appointment_id
            ORDER BY a.date_time;
        """;

        List<AppointmentRow> results = new ArrayList<>();

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            // Keep the table usable even if a related record is missing.
            while (rs.next()) {
                AppointmentRow row = new AppointmentRow();
                row.appointmentId = rs.getInt("appointment_id");
                row.ownerName = rs.getString("owner_name");
                row.regNumber = rs.getString("reg_number");
                row.vehicle = rs.getString("vehicle");
                row.service = rs.getString("service");
                row.dateTime = rs.getString("date_time");
                row.status = rs.getString("status");
                row.paymentMethod = rs.getString("payment_method");
                row.paymentStatus = rs.getString("payment_status");
                row.notes = rs.getString("notes");

                results.add(row);
            }
            
        }

        return results;
    }

        public int createAppointment(int ownerId, int vehicleId, int serviceTypeId, String dateTime, String location, String notes, String status)
            throws SQLException {
        return createAppointment(ownerId, vehicleId, serviceTypeId, dateTime, location, notes, status, "Cash");
        }

        public int createAppointment(int ownerId, int vehicleId, int serviceTypeId, String dateTime, String location, String notes, String status, String paymentMethod)
            throws SQLException{

                String appointmentSql = """
                        INSERT INTO appointment(owner_id, vehicle_id, service_type_id, date_time, status, location, notes)
                        VALUES(?,?,?,?,?,?,?)
                        """;

                String paymentSql = """
                    INSERT INTO payment(appointment_id, method, payment_status, amount)
                    VALUES(?,?,?,?)
                    """;

                try (Connection conn = DBConnection.getConnection()) {
                    boolean previousAutoCommit = conn.getAutoCommit();
                    conn.setAutoCommit(false);

                    try {
                        // Use one transaction so the appointment and payment stay in sync.
                        int newAppointmentId;

                        try (PreparedStatement ps = conn.prepareStatement(appointmentSql, Statement.RETURN_GENERATED_KEYS)) {
                            ps.setInt(1, ownerId);
                            ps.setInt(2, vehicleId);
                            ps.setInt(3, serviceTypeId);
                            ps.setString(4, dateTime);
                            ps.setString(5, (status == null || status.isBlank()) ? "Booked" : status);
                            ps.setString(6, (location == null || location.isBlank()) ? "Main Garage" : location);
                            ps.setString(7, notes);

                            int affected = ps.executeUpdate();
                            if (affected == 0) {
                                throw new SQLException("Creating appointment failed, no rows affected.");
                            }

                            try (ResultSet keys = ps.getGeneratedKeys()) {
                                if (!keys.next()) {
                                    throw new SQLException("Creating appointment failed, no ID obtained.");
                                }
                                newAppointmentId = keys.getInt(1);
                            }
                        }

                        try (PreparedStatement paymentPs = conn.prepareStatement(paymentSql)) {
                            String normalizedPaymentMethod = (paymentMethod == null || paymentMethod.isBlank())
                                    ? "Cash"
                                    : paymentMethod.trim();
                            paymentPs.setInt(1, newAppointmentId);
                            paymentPs.setString(2, normalizedPaymentMethod);
                            paymentPs.setString(3, "Pending");
                            paymentPs.setDouble(4, 0.0d);
                            paymentPs.executeUpdate();
                        }

                        conn.commit();
                        return newAppointmentId;
                    } catch (SQLException ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(previousAutoCommit);
                    }
                }
    }

    public List<LookupOption> getOwnerOptions() throws SQLException {
        // These small lookup lists are used to fill the create form drop-downs.
        String sql = "SELECT owner_id, full_name AS label FROM car_owner ORDER BY full_name";
        return loadLookupOptions(sql, "owner_id", "label");
    }

    public List<LookupOption> getVehicleOptions() throws SQLException {
        // I combined reg number and make/model so the list is easier to read.
        String sql = """
                SELECT
                    vehicle_id,
                    COALESCE(reg_number, '[No Reg]') || ' - ' || COALESCE(make, '') || ' ' || COALESCE(model, '') AS label
                FROM vehicle
                ORDER BY reg_number, make, model
                """;
        return loadLookupOptions(sql, "vehicle_id", "label");
    }

    public List<LookupOption> getServiceTypeOptions() throws SQLException {
        // Service types are sorted alphabetically for a cleaner dropdown.
        String sql = "SELECT service_type_id, name AS label FROM service_type ORDER BY name";
        return loadLookupOptions(sql, "service_type_id", "label");
    }

    private List<LookupOption> loadLookupOptions(String sql, String idColumn, String labelColumn) throws SQLException 
    {
        List<LookupOption> options = new ArrayList<>();

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                options.add(new LookupOption(rs.getInt(idColumn), rs.getString(labelColumn)));
            }
        }

        return options;
    }

    public List<OwnerItem> getAllOwners() throws SQLException {
    String sql = "SELECT owner_id, full_name FROM car_owner ORDER BY full_name;";
    List<OwnerItem> list = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(new OwnerItem(
                    rs.getInt("owner_id"),
                    rs.getString("full_name")
            ));
        }
    }
    return list;
}

public List<VehicleItem> getAllVehicles() throws SQLException {
    String sql = "SELECT vehicle_id, make, model, reg_number FROM vehicle ORDER BY reg_number;";
    List<VehicleItem> list = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String label = rs.getString("reg_number") + " - " +
                           rs.getString("make") + " " +
                           rs.getString("model");

            list.add(new VehicleItem(
                    rs.getInt("vehicle_id"),
                    label
            ));
        }
    }
    return list;
}

public List<ServiceTypeItem> getAllServiceTypes() throws SQLException {
    String sql = "SELECT service_type_id, name FROM service_type ORDER BY name;";
    List<ServiceTypeItem> list = new ArrayList<>();

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(new ServiceTypeItem(
                    rs.getInt("service_type_id"),
                    rs.getString("name")
            ));
        }
    }
    return list;
}

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) throws SQLException{
        // This is used by the status buttons in the main screen.
        String sql= "UPDATE appointment SET status=? WHERE appointment_id=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps= conn.prepareStatement(sql)){
                ps.setString(1, newStatus);
                ps.setInt(2, appointmentId);

                return ps.executeUpdate()>0;
        }
    }

    public boolean updateAppointmentDateTime(int appointmentId, String newDateTime) throws SQLException{
        // Same idea here, just changing the date and time instead.
        String sql= "UPDATE appointment SET date_time=? WHERE appointment_id=?";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement ps= conn.prepareStatement(sql)){
                ps.setString(1, newDateTime);
                ps.setInt(2, appointmentId);

                return ps.executeUpdate()>0;
        }
    }

    public boolean cancelAppointment(int appointmentId) throws SQLException{
        return updateAppointmentStatus(appointmentId, "Cancelled");
    }

    public boolean rescheduleAppointmentDate(int appointmentId, String newDate) throws SQLException {
        // I kept this separate because the UI calls it directly.
        String sql = "UPDATE appointment SET date_time = ? WHERE appointment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newDate);
            ps.setInt(2, appointmentId);

            return ps.executeUpdate() > 0;
        }
    }

}
