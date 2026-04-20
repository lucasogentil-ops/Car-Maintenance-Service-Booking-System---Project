// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

import dao.AppointmentDAO;

public class TestCreateAppointment {
    public static void main(String[]args){
        try{
            AppointmentDAO dao=new AppointmentDAO();

            // Create appointments with different owner IDs
            int[] ownerIds = {1, 2, 3};
            int[] vehicleIds = {1, 2, 3};
            
            for (int i = 0; i < ownerIds.length; i++) {
                // I match the owner and vehicle by index to keep the test simple.
                int ownerId = ownerIds[i];
                int vehicleId = vehicleIds[i];
                int serviceTypeId = 2;
                
                // I kept the date format the same as the rest of the project.
                String dateTime = "2026-07-" + String.format("%02d", 15 + i) + " 10:30:00";
                String location = "Main Garage";
                String notes = "Customer need tyre replacement.";
                String status = "Booked";

                int newId = dao.createAppointment(ownerId, vehicleId, serviceTypeId, dateTime, location, notes, status);
                System.out.println("New appointment created with ID: " + newId);
            }
            // Display all appointments to verify the new entries
            var list=dao.getAllAppointments();
            System.out.println("\nAppointments found:"+list.size());
            // This printout helps me check if the insert worked properly.
            for (var a: list){
                System.out.println(
                    a.appointmentId+" | "+
                    a.ownerName+" | "+
                    a.regNumber+" | "+
                    a.vehicle+" | "+
                    a.service+" | "+
                    a.dateTime+" | "+
                    a.status
                );
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
