package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.team.Team;
import catering.util.LogManager;

/**
 * Manager class for handling staff-related operations including
 * staff lists, contracts, and vacation requests
 */
public class StaffManager {
    private static final Logger LOGGER = LogManager.getLogger(StaffManager.class);
    
    private StaffList currentStaffList;
    private ArrayList<StaffEventReceiver> eventReceivers;

    /**
     * Constructor initializes the staff manager
     */
    public StaffManager() {
        this.eventReceivers = new ArrayList<>();
        LOGGER.info("StaffManager initialized");
    }

    /**
     * Adds an event receiver to listen for staff events
     * @param receiver The event receiver to add
     */
    public void addEventReceiver(StaffEventReceiver receiver) {
        if (!eventReceivers.contains(receiver)) {
            eventReceivers.add(receiver);
            LOGGER.info("Added staff event receiver: " + receiver.getClass().getSimpleName());
        }
    }

    /**
     * Removes an event receiver
     * @param receiver The event receiver to remove
     */
    public void removeEventReceiver(StaffEventReceiver receiver) {
        eventReceivers.remove(receiver);
        LOGGER.info("Removed staff event receiver: " + receiver.getClass().getSimpleName());
    }

    /**
     * Creates a new staff list
     * @param name The name of the staff list
     * @return The created staff list
     * @throws StaffException if creation fails
     */
    public StaffList createStaffList(String name) throws StaffException {
        if (name == null || name.trim().isEmpty()) {
            throw new StaffException("Staff list name cannot be null or empty");
        }
        
        StaffList staffList = new StaffList(name.trim());
        this.currentStaffList = staffList;
        
        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateStaffListCreated(staffList);
        }
        
        LOGGER.info("Created new staff list: " + name);
        return staffList;
    }

    /**
     * Adds an employee to the current staff list
     * @param employee The employee to add
     * @throws StaffException if no current staff list or add fails
     */
    public void addEmployeeToStaffList(Employee employee) throws StaffException {
        if (currentStaffList == null) {
            throw new StaffException("No current staff list selected");
        }
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        
        if (currentStaffList.addEmployee(employee)) {
            // Notify event receivers
            for (StaffEventReceiver receiver : eventReceivers) {
                receiver.updateStaffListAdded(currentStaffList);
            }
            LOGGER.info("Added employee " + employee.getName() + " to current staff list");
        } else {
            LOGGER.warning("Employee " + employee.getName() + " already in staff list");
        }
    }

    /**
     * Creates a new contract for an employee
     * @param contract The contract to create
     * @throws StaffException if contract creation fails
     */
    public void createContract(Contract contract) throws StaffException {
        if (contract == null) {
            throw new StaffException("Contract cannot be null");
        }
        
        contract.saveContract();
        LOGGER.info("Created contract for employee: " + contract.getEmployee().getName());
    }

    /**
     * Deletes a contract
     * @param contract The contract to delete
     * @throws StaffException if deletion fails
     */
    public void deleteContract(Contract contract) throws StaffException {
        if (contract == null) {
            throw new StaffException("Contract cannot be null");
        }
        
        contract.deleteContract();
        
        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateContractDeleted(contract);
        }
        
        LOGGER.info("Deleted contract for employee: " + contract.getEmployee().getName());
    }

    /**
     * Adds a permanent employee to a team
     * @param employee The employee to add
     * @param team The team to add the employee to
     * @throws StaffException if the operation fails
     */
    public void addPermanentEmployeeToTeam(Employee employee, Team team) throws StaffException {
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        if (team == null) {
            throw new StaffException("Team cannot be null");
        }
        
        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updatePermanentEmployeeAdded(employee, team);
        }
        
        LOGGER.info("Added permanent employee " + employee.getName() + " to team " + team.getName());
    }

    /**
     * Evaluates a vacation request
     * @param vacation The vacation request to evaluate
     * @param approved Whether the request is approved or denied
     * @throws StaffException if evaluation fails
     */
    public void evaluateVacationRequest(Vacation vacation, boolean approved) throws StaffException {
        if (vacation == null) {
            throw new StaffException("Vacation request cannot be null");
        }
        
        vacation.setApproved(approved);
        vacation.updateVacation();
        
        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateEvaluateVacationRequest(vacation);
        }
        
        LOGGER.info("Evaluated vacation request for employee: " + vacation.getEmployee().getName() + 
                   " - " + (approved ? "APPROVED" : "DENIED"));
    }

    /**
     * Modifies employee data
     * @param employee The employee with updated data
     * @throws StaffException if modification fails
     */
    public void modifyEmployeeData(Employee employee) throws StaffException {
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        
        employee.updateEmployee();
        
        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateEmployeeDataModified(employee);
        }
        
        LOGGER.info("Modified data for employee: " + employee.getName());
    }

    /**
     * Gets the current staff list
     * @return The current staff list or null if none selected
     */
    public StaffList getCurrentStaffList() {
        return currentStaffList;
    }

    /**
     * Sets the current staff list
     * @param staffList The staff list to set as current
     */
    public void setCurrentStaffList(StaffList staffList) {
        this.currentStaffList = staffList;
        LOGGER.info("Set current staff list: " + (staffList != null ? staffList.getName() : "null"));
    }

    /**
     * Gets all staff lists
     * @return List of all staff lists
     * @throws StaffException if loading fails
     */
    public ArrayList<StaffList> getAllStaffLists() throws StaffException {
        return StaffList.loadAllStaffLists();
    }

    /**
     * Gets all contracts
     * @return List of all contracts
     * @throws StaffException if loading fails
     */
    public ArrayList<Contract> getAllContracts() throws StaffException {
        return Contract.loadAllContracts();
    }
}
