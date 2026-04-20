// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

import dao.AppointmentDAO;
public class TestAppointments {

    public static void main(String[] args){
        try {
            AppointmentDAO dao=new AppointmentDAO();
            var list=dao.getAllAppointments();

            System.out.println("Appointments found:"+list.size());

            for(var a:list){
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
