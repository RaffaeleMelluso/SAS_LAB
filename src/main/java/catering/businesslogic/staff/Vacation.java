package catering.businesslogic.staff;

import java.util.Date;
import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Represents a vacation request from an employee
 */
public class Vacation {
    private static final Logger LOGGER = LogManager.getLogger(Vacation.class);
    
    private int id;
    private Date startDate;
    private Date endDate;
    private String reason;
    private boolean approved;
    private Employee employee;

    /**
     * Default constructor
     */
    public Vacation() {
        LOGGER.fine("Created new empty Vacation");
    }

    /**
     * Constructor for creating a vacation request
     * @param startDate The start date of the vacation
     * @param endDate The end date of the vacation
     * @param reason The reason for the vacation
     * @param employee The employee requesting vacation
     * @throws StaffException if parameters are invalid
     */
    public Vacation(Date startDate, Date endDate, String reason, Employee employee) throws StaffException {
        if (startDate == null) {
            throw new StaffException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new StaffException("End date cannot be null");
        }
        if (startDate.after(endDate)) {
            throw new StaffException("Start date cannot be after end date");
        }
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.employee = employee;
        this.approved = false; // Default to not approved
        
        LOGGER.info("Created vacation request for employee: " + employee.getName());
    }

    /**
     * Saves this vacation request to the database
     * @throws StaffException if the save operation fails
     */
    public void saveVacation() throws StaffException {
        try {
            if (id == 0) {
                String query = "INSERT INTO VacationRequests (start_date, end_date, reason, approved, employee_id) VALUES (?, ?, ?, ?, ?)";
                Long startTimestamp = startDate.getTime();
                Long endTimestamp = endDate.getTime();
                int employeeId = employee.getId();
                
                PersistenceManager.executeUpdate(query, startTimestamp, endTimestamp, reason, approved, employeeId);
                this.id = PersistenceManager.getLastId();
            } else {
                updateVacation();
            }
            LOGGER.info("Successfully saved vacation request with ID: " + this.id);
        } catch (Exception e) {
            LOGGER.severe("Failed to save vacation request: " + e.getMessage());
            throw new StaffException("Failed to save vacation request", e);
        }
    }

    /**
     * Updates this vacation request in the database
     * @throws StaffException if the update operation fails
     */
    public void updateVacation() throws StaffException {
        if (id <= 0) {
            throw new StaffException("Cannot update vacation request that hasn't been saved");
        }
        
        try {
            String query = "UPDATE VacationRequests SET start_date = ?, end_date = ?, reason = ?, approved = ?, employee_id = ? WHERE id = ?";
            Long startTimestamp = startDate.getTime();
            Long endTimestamp = endDate.getTime();
            int employeeId = employee.getId();
            
            PersistenceManager.executeUpdate(query, startTimestamp, endTimestamp, reason, approved, employeeId, id);
            LOGGER.info("Successfully updated vacation request with ID: " + this.id);
        } catch (Exception e) {
            LOGGER.severe("Failed to update vacation request: " + e.getMessage());
            throw new StaffException("Failed to update vacation request", e);
        }
    }

    /**
     * Loads all vacation requests from the database
     * @return List of all vacation requests
     * @throws StaffException if the load operation fails
     */
    public static ArrayList<Vacation> loadAllVacations() throws StaffException {
        ArrayList<Vacation> vacations = new ArrayList<>();
        
        try {
            String query = "SELECT * FROM VacationRequests";
            
            PersistenceManager.executeQuery(query, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    Vacation vacation = new Vacation();
                    vacation.id = rs.getInt("id");
                    vacation.reason = rs.getString("reason");
                    vacation.approved = rs.getBoolean("approved");
                    
                    long startTimestamp = rs.getLong("start_date");
                    if (!rs.wasNull()) {
                        vacation.startDate = new Date(startTimestamp);
                    }
                    
                    long endTimestamp = rs.getLong("end_date");
                    if (!rs.wasNull()) {
                        vacation.endDate = new Date(endTimestamp);
                    }
                    
                    int employeeId = rs.getInt("employee_id");
                    if (!rs.wasNull()) {
                        vacation.employee = Employee.loadById(employeeId);
                    }
                    
                    vacations.add(vacation);
                }
            });
            
            LOGGER.info("Successfully loaded " + vacations.size() + " vacation requests");
        } catch (Exception e) {
            LOGGER.severe("Failed to load vacation requests: " + e.getMessage());
            throw new StaffException("Failed to load vacation requests", e);
        }
        
        return vacations;
    }

    // Getters and setters with documentation

    /**
     * Gets the ID of this vacation request
     * @return The vacation request ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the start date of the vacation
     * @return The start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date of the vacation
     * @param startDate The start date to set
     * @throws StaffException if the date is invalid
     */
    public void setStartDate(Date startDate) throws StaffException {
        if (startDate == null) {
            throw new StaffException("Start date cannot be null");
        }
        if (endDate != null && startDate.after(endDate)) {
            throw new StaffException("Start date cannot be after end date");
        }
        this.startDate = startDate;
    }

    /**
     * Gets the end date of the vacation
     * @return The end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date of the vacation
     * @param endDate The end date to set
     * @throws StaffException if the date is invalid
     */
    public void setEndDate(Date endDate) throws StaffException {
        if (endDate == null) {
            throw new StaffException("End date cannot be null");
        }
        if (startDate != null && endDate.before(startDate)) {
            throw new StaffException("End date cannot be before start date");
        }
        this.endDate = endDate;
    }

    /**
     * Gets the reason for the vacation
     * @return The vacation reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason for the vacation
     * @param reason The reason to set
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Checks if the vacation request is approved
     * @return true if approved, false otherwise
     */
    public boolean isApproved() {
        return approved;
    }

    /**
     * Sets the approval status of the vacation request
     * @param approved The approval status to set
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * Gets the employee who requested the vacation
     * @return The employee
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Sets the employee for this vacation request
     * @param employee The employee to set
     * @throws StaffException if employee is null
     */
    public void setEmployee(Employee employee) throws StaffException {
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        this.employee = employee;
    }

    @Override
    public String toString() {
        return "Vacation{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", reason='" + reason + '\'' +
                ", approved=" + approved +
                ", employee=" + (employee != null ? employee.getName() : "null") +
                '}';
    }
}
