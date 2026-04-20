// Student name: Lucas De Oliveira
// Student Number: C00298828
// Date: April 2026

package Model;

public class AppointmentCreateRequest {

    public int ownerId;
    public int vehicleId;
    public int serviceTypeId;
    public String dateTime; //"YYYY-MM-DD HH:MM:SS"
    public String location; // e.g., "Main Garage"
    public String notes;    // Additional notes
    public String status;   // e.g., "Scheduled", "Completed", "Cancelled"
    
}
