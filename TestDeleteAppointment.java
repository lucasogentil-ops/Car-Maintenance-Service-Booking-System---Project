// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

import dao.AppointmentDAO;
public class TestDeleteAppointment {

    public static void main(String[] args){
        try{
            AppointmentDAO appointmentDAO=new AppointmentDAO();
            int appointmentIDToCancel=6; // ID of the appointment to cancel
            // Attempt to cancel the appointment and print the result
            boolean canceled=appointmentDAO.cancelAppointment(appointmentIDToCancel);
            System.out.println("Appointment canceled: " + canceled);
            // Display all appointments to verify the cancellation
            var list=appointmentDAO.getAllAppointments();
            for( var a:list){
                System.out.println(a.appointmentId+ " | " + a.service + " | " + a.dateTime + " | " + a.status);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
}
