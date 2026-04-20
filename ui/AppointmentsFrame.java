// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

package ui;

import dao.AppointmentDAO;
import dao.AppointmentDAO.AppointmentRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class AppointmentsFrame extends JFrame {

    private static final Color APP_BG = new Color(242, 245, 249);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color HEADER_BG = new Color(28, 43, 60);
    private static final Color HEADER_FG = new Color(245, 248, 252);
    private static final Color TABLE_GRID = new Color(226, 231, 237);
    private static final Color ROW_ALT = new Color(248, 250, 252);
    private static final Color PRIMARY = new Color(41, 98, 255);
    private static final Color SUCCESS = new Color(43, 122, 79);
    private static final Color WARNING = new Color(193, 115, 0);
    private static final Color DANGER = new Color(176, 42, 55);
    private static final Color DIALOG_BG = new Color(250, 252, 255);
    private static final Color DIALOG_LABEL = new Color(41, 56, 73);

    private final AppointmentDAO dao = new AppointmentDAO();
    private final DefaultTableModel model;
    private final JTable table;

    public AppointmentsFrame() {
        setTitle("Car Maintenance Booking System - Appointments");
        setSize(1200, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(APP_BG);
        setLayout(new BorderLayout(12, 12));

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(APP_BG);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {
                "ID", "Owner", "Reg", "Vehicle", "Service",
                "Date/Time", "Status", "Payment Method", "Payment Status", "Notes"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        // The table is set up before loading data so the layout is ready first.
        styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(TABLE_GRID));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JButton btnUpdateStatus = createStyledButton("Mark as Completed", SUCCESS, Color.WHITE);
        JButton btnReschedule = createStyledButton("Reschedule", WARNING, Color.WHITE);
        JButton btnCancel = createStyledButton("Cancel Appointment", DANGER, Color.WHITE);
        JButton btnRefresh = createStyledButton("Refresh", new Color(95, 107, 120), Color.WHITE);
        JButton btnCreate = createStyledButton("Create Appointment", PRIMARY, Color.WHITE);

        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        btnUpdateStatus.addActionListener(e -> markCompleted());
        btnReschedule.addActionListener(e -> showRescheduleDialog());
        btnCancel.addActionListener(e -> cancelSelectedAppointment());

        // Refresh and create are the two actions I use most while testing.
        btnRefresh.addActionListener(e -> loadAppointments());
        btnCreate.addActionListener(e -> showCreateDialog());

        JLabel lblTitle = new JLabel("Appointments Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(HEADER_FG);

        JLabel lblSubtitle = new JLabel("Track bookings, status, and customer service activity");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(210, 220, 231));

        JPanel titlePanel = new JPanel(new GridLayout(0, 1, 0, 4));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        top.add(btnRefresh);
        top.add(btnCreate);
        top.add(btnUpdateStatus);
        top.add(btnReschedule);
        top.add(btnCancel);

        JPanel headerPanel = new JPanel(new BorderLayout(0, 14));
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(18, 18, 18, 18));
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(top, BorderLayout.SOUTH);

        // The card wrapper makes the table area look a bit cleaner.
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(PANEL_BG);
        tableCard.setBorder(new EmptyBorder(12, 12, 12, 12));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        root.add(headerPanel, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        add(root, BorderLayout.CENTER);

        loadAppointments();
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleTable(JTable table) {
        // I kept the table styling simple so the main data stands out more.
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setGridColor(TABLE_GRID);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(220, 234, 255));
        table.setSelectionForeground(new Color(16, 33, 52));

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(236, 241, 247));
        header.setForeground(new Color(32, 49, 68));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_GRID));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    c.setForeground(new Color(40, 52, 66));
                }
                return c;
            }
        });
    }

    private Integer getSelectedAppointmentId() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.");
            return null;
        }
        Object value = table.getValueAt(selectedRow, 0); // ID column
        return Integer.parseInt(value.toString());
    }

    private void markCompleted() {
        Integer appointmentId = getSelectedAppointmentId();
        if (appointmentId == null) {
            return;
        }

        try {
            dao.updateAppointmentStatus(appointmentId, "Completed");
            JOptionPane.showMessageDialog(this, "Appointment marked as Completed.");
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating appointment status:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showRescheduleDialog() {
        Integer appointmentId = getSelectedAppointmentId();
        if (appointmentId == null) {
            return;
        }

        // I use a small form here so the user only edits the date and time.
        JTextField txtDateTime = createFormTextField("2026-02-15 14:00");
        JPanel panel = createFormPanel();
        addFormRow(panel, "New Date/Time (YYYY-MM-DD HH:MM):", txtDateTime);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Reschedule Appointment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this, "No appointment was rescheduled.");
            return;
        }

        String newDate = txtDateTime.getText().trim();

        if (newDate == null || newDate.isBlank()) {
            JOptionPane.showMessageDialog(this, "No appointment was rescheduled.");
            return;
        }

        try {
            dao.rescheduleAppointmentDate(appointmentId, newDate.trim());
            JOptionPane.showMessageDialog(this, "Appointment rescheduled.");
            loadAppointments();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error rescheduling appointment:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void cancelSelectedAppointment() {
        Integer appointmentId = getSelectedAppointmentId();
        if (appointmentId == null) {
            return;
        }

        // This confirmation avoids deleting the appointment by mistake.
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean ok = dao.cancelAppointment(appointmentId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Appointment cancelled successfully.");
                loadAppointments();
            } else {
                JOptionPane.showMessageDialog(this, "No appointment was cancelled");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error cancelling appointment:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadAppointments() {
        try {
            // Reloading everything is easier than updating rows one by one here.
            model.setRowCount(0);
            List<AppointmentRow> list = dao.getAllAppointments();

            for (AppointmentRow a : list) {
                model.addRow(new Object[]{
                        a.appointmentId,
                        a.ownerName,
                        a.regNumber,
                        a.vehicle,
                        a.service,
                        a.dateTime,
                        a.status,
                        a.paymentMethod,
                        a.paymentStatus,
                        a.notes
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading appointments:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showCreateDialog() {
        try {
            // Load the dropdowns from the database so the user picks valid records.
            JComboBox<AppointmentDAO.OwnerItem> cmbOwner = createFormComboBox();
            JComboBox<AppointmentDAO.VehicleItem> cmbVehicle = createFormComboBox();
            JComboBox<AppointmentDAO.ServiceTypeItem> cmbService = createFormComboBox();
            JComboBox<String> cmbPaymentMethod = createFormComboBox();
            cmbPaymentMethod.addItem("Card");
            cmbPaymentMethod.addItem("Cash");

            // These loops fill the drop-downs with records from the database.
            for (AppointmentDAO.OwnerItem owner : dao.getAllOwners()) {
                cmbOwner.addItem(owner);
            }

            for (AppointmentDAO.VehicleItem vehicle : dao.getAllVehicles()) {
                cmbVehicle.addItem(vehicle);
            }

            for (AppointmentDAO.ServiceTypeItem service : dao.getAllServiceTypes()) {
                cmbService.addItem(service);
            }

            JTextField txtDateTime = createFormTextField("2026-02-10 10:00");
            JTextField txtStatus = createFormTextField("Booked");
            JTextField txtLocation = createFormTextField("Main Garage");
            JTextField txtNotes = createFormTextField("");

            // I kept the fields in a simple order so the form is easy to follow.
            JPanel panel = createFormPanel();
            addFormRow(panel, "Owner:", cmbOwner);
            addFormRow(panel, "Vehicle:", cmbVehicle);
            addFormRow(panel, "Service Type:", cmbService);
            addFormRow(panel, "Date/Time (YYYY-MM-DD HH:MM):", txtDateTime);
            addFormRow(panel, "Status:", txtStatus);
            addFormRow(panel, "Payment Method:", cmbPaymentMethod);
            addFormRow(panel, "Location:", txtLocation);
            addFormRow(panel, "Notes:", txtNotes);

            int result = JOptionPane.showConfirmDialog(
                    this,
                    panel,
                    "Create Appointment",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                AppointmentDAO.OwnerItem selectedOwner =
                        (AppointmentDAO.OwnerItem) cmbOwner.getSelectedItem();
                AppointmentDAO.VehicleItem selectedVehicle =
                        (AppointmentDAO.VehicleItem) cmbVehicle.getSelectedItem();
                AppointmentDAO.ServiceTypeItem selectedService =
                        (AppointmentDAO.ServiceTypeItem) cmbService.getSelectedItem();

                if (selectedOwner == null || selectedVehicle == null || selectedService == null) {
                    JOptionPane.showMessageDialog(this, "Please select owner, vehicle, and service.");
                    return;
                }

                // Basic checks stop empty values from reaching the database.
                String dateTime = txtDateTime.getText().trim();
                String status = txtStatus.getText().trim();
                String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();
                String location = txtLocation.getText().trim();
                String notes = txtNotes.getText().trim();

                if (dateTime.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Date/Time cannot be empty.");
                    return;
                }

                if (paymentMethod == null || paymentMethod.isBlank()) {
                    JOptionPane.showMessageDialog(this, "Please select a payment method.");
                    return;
                }

                int newId = dao.createAppointment(
                        selectedOwner.id,
                        selectedVehicle.id,
                        selectedService.id,
                        dateTime,
                        location,
                        notes,
                        status,
                        paymentMethod
                );

                JOptionPane.showMessageDialog(this,
                        "Appointment created successfully! ID: " + newId);

                loadAppointments();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error opening create dialog:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBackground(DIALOG_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private void addFormRow(JPanel panel, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(DIALOG_LABEL);
        panel.add(label);
        panel.add(field);
    }

    private JTextField createFormTextField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TABLE_GRID),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private <T> JComboBox<T> createFormComboBox() {
        JComboBox<T> comboBox = new JComboBox<>();
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(TABLE_GRID));
        return comboBox;
    }
}