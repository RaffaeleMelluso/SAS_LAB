package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import catering.businesslogic.CatERing;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.team.Team;
import catering.businesslogic.team.TeamException;
import catering.businesslogic.team.TeamManager;
import catering.businesslogic.user.User;
import catering.util.LogManager;

/**
 * Manager class for handling staff-related operations including
 * staff lists, contracts, and vacation requests.
 */
public class StaffManager {
    private static final Logger LOGGER = LogManager.getLogger(StaffManager.class);

    private StaffList currentStaffList;
    private ArrayList<StaffEventReceiver> eventReceivers;

    /**
     * Constructor initializes the staff manager.
     */
    public StaffManager() {
        this.eventReceivers = new ArrayList<>();
        LOGGER.info("StaffManager initialized");
    }

    /**
     * Adds an event receiver to listen for staff events.
     * @param receiver The event receiver to add
     */
    public void addEventReceiver(StaffEventReceiver receiver) {
        if (!eventReceivers.contains(receiver)) {
            eventReceivers.add(receiver);
            LOGGER.info("Added staff event receiver: " + receiver.getClass().getSimpleName());
        }
    }

    /**
     * Removes an event receiver.
     * @param receiver The event receiver to remove
     */
    public void removeEventReceiver(StaffEventReceiver receiver) {
        eventReceivers.remove(receiver);
        LOGGER.info("Removed staff event receiver: " + receiver.getClass().getSimpleName());
    }

    /**
     * Creates a new staff list. Only owners can create staff lists.
     * @param name The name of the staff list
     * @param owner The owner of the staff list
     * @return The created staff list
     * @throws StaffException if creation fails
     */
    public StaffList createStaffList(String name, User owner) throws StaffException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new StaffException("Only owners can create staff lists");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new StaffException("Staff list name cannot be null or empty");
        }
        if (owner == null) {
            throw new StaffException("Owner cannot be null");
        }

        StaffList staffList = new StaffList(name.trim(), owner);
        this.currentStaffList = staffList;

        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateStaffListCreated(staffList);
        }

        LOGGER.info("Created new staff list: " + name);
        return staffList;
    }

    /**
     * Adds an employee to the current staff list.
     * @param employee The employee to add
     * @param engagementType The engagement type ("permanente" or "occasionale")
     * @throws StaffException if no current staff list or add fails
     */
    public void addEmployeeToStaffList(Employee employee, String engagementType) throws StaffException {
        if (currentStaffList == null) {
            throw new StaffException("No current staff list selected");
        }
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }

        currentStaffList.addEmployee(employee, engagementType);

        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateStaffListAdded(currentStaffList);
        }
        LOGGER.info("Added employee " + employee.getName() + " to current staff list");
    }

    /**
     * Assegna un dipendente permanente a un team.
     * @param employee Il dipendente da assegnare
     * @param team Il team a cui assegnare il dipendente
     * @throws StaffException se l'assegnazione non è valida
     * @throws TeamException se l'assegnazione fallisce lato team
     */
    public void assignPermanentEmployeeToTeam(Employee employee, Team team) throws StaffException, TeamException {
        if (employee == null) throw new StaffException("Employee cannot be null");
        if (team == null) throw new StaffException("Team cannot be null");
        if (currentStaffList == null) throw new StaffException("No current staff list selected");

        String engagementType = currentStaffList.getEngagementType(employee);
        if (engagementType == null)
            throw new StaffException("Employee is not in this staff list");
        if (!"permanente".equals(engagementType))
            throw new StaffException("Only permanent employees can be assigned to teams");

        TeamManager teamManager = CatERing.getInstance().getTeamManager();
        teamManager.setCurrentTeam(team);
        teamManager.addEmployee(employee, engagementType);
        LOGGER.info("Assigned permanent employee " + employee.getName() + " to team " + team.getName());
    }

    /**
     * Creates a new contract. Only owners can create contracts.
     * @param engagement The engagement type ("permanente" or "occasionale")
     * @param startDate The start date of the contract
     * @param endDate The end date of the contract (can be null for permanent contracts)
     * @param employee The employee for whom to create the contract
     * @return The created contract
     * @throws StaffException if creation fails or user lacks permission
     */
    public Contract createContract(String engagement, Date startDate, Date endDate, Employee employee) throws StaffException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new StaffException("Only owners can create contracts");
        }

        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }

        if (engagement == null || engagement.trim().isEmpty()) {
            throw new StaffException("Engagement type cannot be null or empty");
        }

        if (startDate == null) {
            throw new StaffException("Start date cannot be null");
        }

        // Create the contract without specifying the ID (it will be auto-generated)
        Contract contract = new Contract(engagement, startDate, endDate, employee);
        contract.saveContract(); // This will set the ID from the database

        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateContractCreated(contract);
        }

        LOGGER.info("Created new contract for employee: " + employee.getName() + 
                    " with engagement: " + engagement + " (ID: " + contract.getContractId() + ")");
        return contract;
    }

    /**
     * Deletes a contract. Only owners can delete contracts.
     * @param contract The contract to delete
     * @return true if successful, false otherwise
     * @throws StaffException if deletion fails or user lacks permission
     */
    public boolean deleteContract(Contract contract) throws StaffException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new StaffException("Only owners can delete contracts");
        }

        if (contract == null) {
            throw new StaffException("Contract cannot be null");
        }

        boolean success = contract.deleteContract();
        
        if (success) {
            // Notify event receivers
            for (StaffEventReceiver receiver : eventReceivers) {
                receiver.updateContractDeleted(contract);
            }
            LOGGER.info("Deleted contract: " + contract.getContractId());
        } else {
            LOGGER.warning("Failed to delete contract: " + contract.getContractId());
        }

        return success;
    }

    /**
     * Changes contract engagement from occasional to permanent. Only owners can do this.
     * @param contract The contract to modify
     * @throws StaffException if modification fails or user lacks permission
     */
    public void changeContractToPermanent(Contract contract) throws StaffException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new StaffException("Only owners can modify contract engagement");
        }

        if (contract == null) {
            throw new StaffException("Contract cannot be null");
        }

        if (!"occasionale".equals(contract.getEngagement())) {
            throw new StaffException("Contract is not occasional, cannot change to permanent");
        }

        contract.changeEngagementToPermanent();

        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateContractEngagementChanged(contract);
        }

        LOGGER.info("Changed contract engagement to permanent for employee: " + 
                    contract.getEmployee().getName());
    }

    /**
     * Removes an employee from staff list. Only owners can do this.
     * @param employee The employee to remove
     * @throws StaffException if removal fails or user lacks permission
     */
    public void removeEmployeeFromStaffList(Employee employee) throws StaffException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new StaffException("Only owners can remove employees from staff list");
        }

        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }

        if (currentStaffList == null) {
            throw new StaffException("No current staff list selected");
        }

        boolean removed = currentStaffList.removeEmployee(employee);
        
        if (removed) {
            currentStaffList.updateStaffList();
            
            // Notify event receivers
            for (StaffEventReceiver receiver : eventReceivers) {
                receiver.updateEmployeeRemovedFromStaffList(employee, currentStaffList);
            }
            
            LOGGER.info("Removed employee " + employee.getName() + 
                       " from staff list " + currentStaffList.getId());
        } else {
            throw new StaffException("Employee not found in current staff list");
        }
    }

    /**
     * Evaluates vacation requests. Only owners can do this.
     * @param vacation The vacation request to evaluate
     * @param newState The new state ("Accettata", "Rifiutata")
     * @throws StaffException if evaluation fails or user lacks permission
     */
    public void evaluateVacationRequest(Vacation vacation, String newState) throws StaffException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new StaffException("Only owners can evaluate vacation requests");
        }

        if (vacation == null) {
            throw new StaffException("Vacation request cannot be null");
        }

        if (newState == null || newState.trim().isEmpty()) {
            throw new StaffException("New state cannot be null or empty");
        }

        if (!"Accettata".equals(newState) && !"Rifiutata".equals(newState)) {
            throw new StaffException("Invalid state. Must be 'Accettata' or 'Rifiutata'");
        }

        vacation.updateState(newState);

        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateEvaluateVacationRequest(vacation);
        }

        LOGGER.info("Evaluated vacation request " + vacation.getId() + 
                   " with state: " + newState);
    }

    /**
     * Assigns occasional employee to team. Organizers and owners can do this.
     * @param employee The employee to assign
     * @param team The team to assign the employee to
     * @throws StaffException if assignment fails or user lacks permission
     * @throws TeamException if team assignment fails
     */
    public void assignOccasionalEmployeeToTeam(Employee employee, Team team) throws StaffException, TeamException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOrganizer() && !currentUser.isOwner()) {
            throw new StaffException("Only organizers and owners can assign occasional employees to teams");
        }

        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }

        if (team == null) {
            throw new StaffException("Team cannot be null");
        }

        if (currentStaffList == null) {
            throw new StaffException("No current staff list selected");
        }

        // Check if employee is in staff list and is occasional
        String engagement = currentStaffList.getEngagementType(employee);
        if (engagement == null) {
            throw new StaffException("Employee not found in current staff list");
        }

        if (!"occasionale".equals(engagement)) {
            throw new StaffException("Employee is not occasional, cannot assign to team");
        }

        // Use TeamManager to perform the assignment
        CatERing.getInstance().getTeamManager().addEmployee(employee, engagement);

        // Notify event receivers
        for (StaffEventReceiver receiver : eventReceivers) {
            receiver.updateOccasionalEmployeeAssignedToTeam(employee, team);
        }

        LOGGER.info("Assigned occasional employee " + employee.getName() + 
                   " to team " + team.getName());
    }

    /**
     * Gets all contracts for an employee.
     * @param employee The employee to get contracts for
     * @return List of contracts for the employee
     * @throws StaffException if loading fails
     */
    public ArrayList<Contract> getEmployeeContracts(Employee employee) throws StaffException {
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }

        // This would require a method in Contract class to load by employee
        // For now, we'll load all contracts and filter
        ArrayList<Contract> allContracts = Contract.loadAllContracts();
        ArrayList<Contract> employeeContracts = new ArrayList<>();
        
        for (Contract contract : allContracts) {
            if (contract.getEmployee().equals(employee)) {
                employeeContracts.add(contract);
            }
        }
        
        return employeeContracts;
    }

    /**
     * Gets all active contracts.
     * @return List of all active contracts
     * @throws StaffException if loading fails
     */
    public ArrayList<Contract> getActiveContracts() throws StaffException {
        ArrayList<Contract> allContracts = Contract.loadAllContracts();
        ArrayList<Contract> activeContracts = new ArrayList<>();
        
        for (Contract contract : allContracts) {
            if (contract.isActive()) {
                activeContracts.add(contract);
            }
        }
        
        return activeContracts;
    }
}