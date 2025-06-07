package catering.businesslogic.staff;

import java.util.Date;
import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.employee.EmployeeManager;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

/**
 * Represents a vacation request from an employee.
 * Only stores state, startDate, endDate, and employee.
 */
public class Vacation {
    private static final Logger LOGGER = LogManager.getLogger(Vacation.class);

    private int id;
    private String state; // "Non valutata", "Accettata", "Rifiutata"
    private Date startDate;
    private Date endDate;
    private Employee employee;

    public Vacation() {}

    public Vacation(Employee employee, Date startDate, Date endDate) throws StaffException {
        if (employee == null) throw new StaffException("Employee cannot be null");
        if (startDate == null || endDate == null) throw new StaffException("Dates cannot be null");
        if (startDate.after(endDate)) throw new StaffException("Start date cannot be after end date");
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = "Non valutata";
    }

    // ===== GETTERS & SETTERS =====
    public int getId() { return id; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    // ===== PERSISTENCE =====

    public void save() throws StaffException {
        if (employee == null) throw new StaffException("Employee cannot be null");
        if (id == 0) {
            String query = "INSERT INTO Vacations (employee_id, start_date, end_date, state) VALUES (?, ?, ?, ?)";
            PersistenceManager.executeUpdate(query, employee.getId(), startDate.getTime(), endDate.getTime(), state);
            this.id = PersistenceManager.getLastId();
        } else {
            update();
        }
    }

    public void update() throws StaffException {
        if (id == 0) throw new StaffException("Vacation not saved yet");
        String query = "UPDATE Vacations SET employee_id = ?, start_date = ?, end_date = ?, state = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, employee.getId(), startDate.getTime(), endDate.getTime(), state, id);
    }

    /**
     * Deletes this vacation request from the database.
     * 
     * @return true if successful, false otherwise
     * @throws StaffException if the vacation is not yet saved
     */
    public boolean delete() throws StaffException {
        if (id == 0)
            throw new StaffException("Vacation not saved yet");

        String deleteQuery = "DELETE FROM Vacations WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(deleteQuery, id);

        if (rows > 0) {
            LOGGER.info("Deleted vacation request with ID: " + id);
            id = 0;
            return true;
        } else {
            LOGGER.warning("Failed to delete vacation request with ID: " + id);
            return false;
        }
    }

    // ===== BUSINESS LOGIC =====

    /**
     * Updates the state of the vacation request.
     * If accepted, updates the employee's vacation days left.
     * @param newState The new state ("Non valutata", "Accettata", "Rifiutata")
     * @throws StaffException if invalid state or persistence fails
     */
    public void updateState(String newState) throws StaffException {
        if (!"Non valutata".equals(newState) && !"Accettata".equals(newState) && !"Rifiutata".equals(newState)) {
            throw new StaffException("Invalid state: " + newState);
        }
        this.state = newState;
        update();
        LOGGER.info("Vacation " + id + " state updated to: " + newState);

        if ("Accettata".equals(newState)) {
            EmployeeManager.updateVacationDaysLeft(employee, startDate, endDate);
        }
    }
}