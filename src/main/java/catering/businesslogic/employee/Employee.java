package catering.businesslogic.employee;

import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;
import catering.util.LogManager;

/**
 * Employee represents a staff member in the CatERing system.
 * Each employee has personal data and a number of vacation days left.
 * Provides persistence methods for CRUD operations.
 */
public class Employee {
    private static final Logger LOGGER = LogManager.getLogger(Employee.class);

    private int id;
    private String name;
    private String fiscalCode;
    private String email;
    private String phoneNumber;
    private int vacationDaysLeft = 0;

    /**
     * Default constructor for loading from DB.
     */
    private Employee() {
    }

    /**
     * Constructs a new Employee with all fields.
     * 
     * @param name        The employee's name
     * @param fiscalCode  The employee's fiscal code
     * @param email       The employee's email
     * @param phoneNumber The employee's phone number
     */
    public Employee(String name, String fiscalCode, String email, String phoneNumber) {
        this.name = name;
        this.fiscalCode = fiscalCode;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Constructs a new Employee with only name and fiscal code.
     * 
     * @param name       The employee's name
     * @param fiscalCode The employee's fiscal code
     */
    public Employee(String name, String fiscalCode) {
        this.name = name;
        this.fiscalCode = fiscalCode;
    }

    // ===== GETTERS & SETTERS =====

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFiscalCode() {
        return fiscalCode;
    }
    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public int getVacationDaysLeft() {
        return vacationDaysLeft;
    }
    public void setVacationDaysLeft(int vacationDaysLeft) {
        this.vacationDaysLeft = vacationDaysLeft;
    }

    // ===== PERSISTENCE METHODS =====

    /**
     * Loads an employee by ID from the database.
     * 
     * @param id The employee ID
     * @return The loaded Employee or null if not found
     */
    public static Employee loadById(int id) {
        Employee[] holder = new Employee[1];
        String query = "SELECT * FROM Employees WHERE id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Employee e = new Employee();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.fiscalCode = rs.getString("fiscal_code");
                e.email = rs.getString("email");
                e.phoneNumber = rs.getString("phone_number");
                e.vacationDaysLeft = rs.getInt("vacation_days_left");
                holder[0] = e;
            }
        }, id);
        return holder[0];
    }

    /**
     * Loads all employees from the database.
     * 
     * @return List of all employees
     */
    public static ArrayList<Employee> loadAllEmployees() {
        ArrayList<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM Employees";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Employee e = new Employee();
                e.id = rs.getInt("id");
                e.name = rs.getString("name");
                e.fiscalCode = rs.getString("fiscal_code");
                e.email = rs.getString("email");
                e.phoneNumber = rs.getString("phone_number");
                e.vacationDaysLeft = rs.getInt("vacation_days_left");
                employees.add(e);
            }
        });
        return employees;
    }

    /**
     * Saves a new employee to the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean save() {
        if (id != 0) return false; // Already exists
        String query = "INSERT INTO Employees (name, fiscal_code, email, phone_number, vacation_days_left) VALUES (?, ?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query, name, fiscalCode, email, phoneNumber, vacationDaysLeft);
        id = PersistenceManager.getLastId();
        LOGGER.info("Created new employee: " + name + " (ID: " + id + ")");
        return id > 0;
    }

    /**
     * Updates this employee in the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean update() {
        if (id == 0) return false; // Not in DB
        String query = "UPDATE Employees SET name = ?, fiscal_code = ?, email = ?, phone_number = ?, vacation_days_left = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, name, fiscalCode, email, phoneNumber, vacationDaysLeft, id);
        LOGGER.info("Updated employee: " + name + " (ID: " + id + ")");
        return rows > 0;
    }

    /**
     * Deletes this employee from the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean delete() {
        if (id == 0)
            return false; // Not in DB

        String deleteQuery = "DELETE FROM Employees WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(deleteQuery, id);

        if (rows > 0) {
            LOGGER.info("Deleted employee: " + name + " (ID: " + id + ")");
            id = 0;
            return true;
        } else {
            LOGGER.warning("Failed to delete employee: " + name + " (ID: " + id + ")");
            return false;
        }
    }

    /**
     * Determines if this employee is equal to another object.
     * Two employees are considered equal if they have the same ID or, if ID is 0,
     * the same fiscal code and name.
     *
     * @param obj The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Employee other = (Employee) obj;

        // If both employees have valid IDs, compare by ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Otherwise, compare by fiscal code (unique) and name
        boolean codeMatch = (this.fiscalCode == null && other.fiscalCode == null) ||
                (this.fiscalCode != null && this.fiscalCode.equals(other.fiscalCode));
        boolean nameMatch = (this.name == null && other.name == null) ||
                (this.name != null && this.name.equals(other.name));

        return codeMatch && nameMatch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        if (id > 0) {
            result = prime * result + id;
        } else {
            result = prime * result + (fiscalCode != null ? fiscalCode.hashCode() : 0);
            result = prime * result + (name != null ? name.hashCode() : 0);
        }

        return result;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", name=" + name + ", fiscalCode=" + fiscalCode + ", email=" + email +
                ", phoneNumber=" + phoneNumber + ", vacationDaysLeft=" + vacationDaysLeft + "]";
    }
}