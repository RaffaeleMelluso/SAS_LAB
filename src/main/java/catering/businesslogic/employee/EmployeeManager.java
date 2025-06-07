package catering.businesslogic.employee;

import java.util.Date;
import java.util.logging.Logger;
import catering.util.LogManager;

/**
 * EmployeeManager handles all operations related to employees in the CatERing system.
 * It manages employee creation, modification, vacation updates, and loading.
 */
public class EmployeeManager {
    private static final Logger LOGGER = LogManager.getLogger(EmployeeManager.class);

    private Employee currentEmployee;

    /**
     * Gets the current employee.
     * 
     * @return The current employee or null if none selected
     */
    public Employee getCurrentEmployee() {
        return this.currentEmployee;
    }

    /**
     * Creates a new employee with the given data.
     * 
     * @param name             The employee's name
     * @param fiscalCode       The employee's fiscal code
     * @param email            The employee's email
     * @param phoneNumber      The employee's phone number
     * @param vacationDaysLeft The number of vacation days left
     * @return The newly created employee, or null if creation failed
     */
    public Employee createEmployee(String name, String fiscalCode, String email, String phoneNumber, int vacationDaysLeft) {
        Employee employee = new Employee(name, fiscalCode, email, phoneNumber);
        employee.setVacationDaysLeft(vacationDaysLeft);
        if (employee.save()) {
            LOGGER.info("Employee created: " + employee);
            this.currentEmployee = employee;
            return employee;
        } else {
            LOGGER.warning("Failed to create employee: " + name);
            return null;
        }
    }

    /**
     * Modifies the data of the current employee.
     * 
     * @param name        The new name
     * @param fiscalCode  The new fiscal code
     * @param email       The new email
     * @param phoneNumber The new phone number
     */
    public void modifyEmployeeData(String name, String fiscalCode, String email, String phoneNumber) {
        if (currentEmployee != null) {
            currentEmployee.setName(name);
            currentEmployee.setFiscalCode(fiscalCode);
            currentEmployee.setEmail(email);
            currentEmployee.setPhoneNumber(phoneNumber);
            if (currentEmployee.update()) {
                LOGGER.info("Employee data updated: " + currentEmployee);
            } else {
                LOGGER.warning("Failed to update employee data: " + name);
            }
        } else {
            LOGGER.warning("No current employee selected for modification.");
        }
    }


    /**
     * Updates the number of vacation days left for the given employee,
     * subtracting the days between start_date and end_date (inclusive).
     */
    public static void updateVacationDaysLeft(Employee employee, java.util.Date startDate, java.util.Date endDate) {
        if (employee != null && startDate != null && endDate != null) {
            int vacationDays = (int) ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1);
            int newDaysLeft = employee.getVacationDaysLeft() - vacationDays;
            employee.setVacationDaysLeft(newDaysLeft);
            employee.update();
            Logger.getLogger(EmployeeManager.class.getName()).info(
                "Updated vacation days left for employee: " + employee.getName() +
                " (now: " + newDaysLeft + ")");
        }
    }


    /**
     * Loads an employee by ID and sets it as the current employee.
     * 
     * @param id The employee ID
     * @return The loaded employee or null if not found
     */
    public Employee loadEmployeeById(int id) {
        Employee employee = Employee.loadById(id);
        if (employee != null) {
            LOGGER.info("Loaded employee: " + employee);
            this.currentEmployee = employee;
        } else {
            LOGGER.warning("Employee not found with ID: " + id);
        }
        return employee;
    }
}