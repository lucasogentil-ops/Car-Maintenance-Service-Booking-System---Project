// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

import dao.AppointmentDAO;

public class TestUpdateAppointment{
    public static void main(String[] args){
       
        try{ 
            AppointmentDAO appointmentDAO= new AppointmentDAO();
            int appointmentID=2; // ID of the appointment to update
            // Attempt to update the appointment status and reschedule the date, then print the results
            boolean ok1=appointmentDAO.updateAppointmentStatus(appointmentID, "Delayed");
            System.out.println("Appointment status updated: " + ok1);
            // Attempt to reschedule the appointment date and print the result
            boolean ok2=appointmentDAO.rescheduleAppointmentDate(appointmentID, "2026-05-05");
            System.out.println("Rescheduled: " + ok2);

            if (!ok1 && !ok2) {
                System.out.println("No row updated. Check whether appointment_id=" + appointmentID + " exists.");
            }

            var list=appointmentDAO.getAllAppointments();
            for(var a:list){
                System.out.println(a.appointmentId + " " + a.service + " " + a.dateTime + " " + a.status);
        }
    } catch(Exception e){
        e.printStackTrace();
    }
    }

};

