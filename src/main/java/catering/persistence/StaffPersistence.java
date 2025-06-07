package catering.persistence;

import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.staff.*;
import catering.businesslogic.team.Team;
import catering.util.LogManager;

/**
 * Persistence layer for staff-related operations.
 * Handles database operations for staff events.
 */
public class StaffPersistence implements StaffEventReceiver {
    private static final Logger LOGGER = LogManager.getLogger(StaffPersistence.class);

    /**
     * Handles persistence when a staff list is added/updated
     * @param staffList The staff list that was added
     */
    @Override
    public void updateStaffListAdded(StaffList staffList) {
        LOGGER.info("Persisting staff list addition: " + staffList.getId());
        try {
            staffList.saveStaffList();
        } catch (StaffException e) {
            LOGGER.severe("Failed to persist staff list addition: " + e.getMessage());
        }
    }

    /**
     * Handles persistence when a new staff list is created
     * @param staffList The staff list that was created
     */
    @Override
    public void updateStaffListCreated(StaffList staffList) {
        LOGGER.info("Persisting staff list creation: " + staffList.getId());
        try {
            staffList.saveStaffList();
        } catch (StaffException e) {
            LOGGER.severe("Failed to persist staff list creation: " + e.getMessage());
        }
    }

    /**
     * Handles persistence when a contract is deleted
     * @param contract The contract that was deleted
     */
    @Override
    public void updateContractDeleted(Contract contract) {
        LOGGER.info("Persisting contract deletion: " + contract.getContractId());
        try {
            contract.deleteContract();
        } catch (StaffException e) {
            LOGGER.severe("Failed to persist contract deletion: " + e.getMessage());
        }
    }

    /**
     * Handles persistence when a permanent employee is added to a team
     * @param employee The employee that was added
     * @param team The team the employee was added to
     */
    @Override
    public void updatePermanentEmployeeAdded(Employee employee, Team team) {
        LOGGER.info("Persisting permanent employee assignment: " + employee.getName() + 
                   " to team " + team.getName());
        // La persistenza dell'assignment viene gestita dal TeamManager
        // Qui possiamo aggiungere logging o altre operazioni se necessario
    }

    /**
     * Handles persistence when a vacation request is evaluated
     * @param vacation The vacation request that was evaluated
     */
    @Override
    public void updateEvaluateVacationRequest(Vacation vacation) {
        LOGGER.info("Persisting vacation request evaluation: " + vacation.getId());
        try {
            vacation.updateVacation();
        } catch (StaffException e) {
            LOGGER.severe("Failed to persist vacation request evaluation: " + e.getMessage());
        }
    }

    /**
     * Handles persistence when employee data is modified
     * @param employee The employee whose data was modified
     */
    @Override
    public void updateEmployeeDataModified(Employee employee) {
        LOGGER.info("Persisting employee data modification: " + employee.getName());
        // La persistenza dei dati employee viene gestita dall'EmployeeManager
        // Qui possiamo aggiungere logging o altre operazioni se necessario
    }
}
