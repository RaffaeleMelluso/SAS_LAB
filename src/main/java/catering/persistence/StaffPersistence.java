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
        try {
            if (staffList.getId() == 0) staffList.saveStaffList();
            else staffList.updateStaffList();
        } catch (StaffException e) {
            LOGGER.severe("Failed to persist staff list: " + e.getMessage());
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
        // No need to call deleteContract() here because it has already been done in the manager
        // This method serves for additional persistence operations if necessary
    }

    /**
     * Handles persistence when a contract is created
     * @param contract The contract that was created
     */
    @Override
    public void updateContractCreated(Contract contract) {
        LOGGER.info("Persisting contract creation: " + contract.getContractId());
        // Contract is already saved in the manager, here we can do additional logging
        // or other persistence operations if necessary
    }

    /**
     * Handles persistence when a contract's engagement is changed
     * @param contract The contract whose engagement was changed
     */
    @Override
    public void updateContractEngagementChanged(Contract contract) {
        LOGGER.info("Persisting contract engagement change: " + contract.getContractId());
        // Contract is already updated in the manager
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
        // The employee is already added to the team in the manager
    }

    /**
     * Handles persistence when a vacation request is evaluated.
     * @param vacation The vacation request that was evaluated
     */
    @Override
    public void updateEvaluateVacationRequest(Vacation vacation) {
        LOGGER.info("Persisting vacation request evaluation: " + vacation.getId());
        // La vacation è già stata aggiornata nel manager
    }

    /**
     * Handles persistence when employee data is modified
     * @param employee The employee whose data was modified
     */
    @Override
    public void updateEmployeeDataModified(Employee employee) {
        LOGGER.info("Persisting employee data modification: " + employee.getName());
        try {
            employee.update();
        } catch (Exception e) {
            LOGGER.severe("Failed to persist employee data modification: " + e.getMessage());
        }
    }

    /**
     * Handles persistence when an employee is removed from a staff list
     * @param employee The employee that was removed
     * @param staffList The staff list the employee was removed from
     */
    @Override
    public void updateEmployeeRemovedFromStaffList(Employee employee, StaffList staffList) {
        LOGGER.info("Persisting employee removal from staff list: " + employee.getName() + 
                   " from list " + staffList.getId());
        // La staffList è già stata aggiornata nel manager
    }

    /**
     * Handles persistence when an occasional employee is assigned to a team
     * @param employee The employee that was assigned
     * @param team The team the employee was assigned to
     */
    @Override
    public void updateOccasionalEmployeeAssignedToTeam(Employee employee, Team team) {
        LOGGER.info("Persisting occasional employee assignment: " + employee.getName() + 
                   " to team " + team.getName());
    }
}