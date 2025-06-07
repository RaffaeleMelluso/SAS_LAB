package catering.businesslogic.staff;

import java.util.ArrayList;
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
     * Creates a new staff list with the given name and owner.
     * @param name The name of the staff list
     * @param owner The owner of the staff list
     * @return The created staff list
     * @throws StaffException if creation fails
     */
    public StaffList createStaffList(String name, User owner) throws StaffException {
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
     * Gets the current staff list.
     * @return The current staff list or null if none selected
     */
    public StaffList getCurrentStaffList() {
        return currentStaffList;
    }

    /**
     * Sets the current staff list.
     * @param staffList The staff list to set as current
     */
    public void setCurrentStaffList(StaffList staffList) {
        this.currentStaffList = staffList;
        LOGGER.info("Set current staff list: " + (staffList != null ? staffList.toString() : "null"));
    }

    /**
     * Gets all staff lists.
     * @return List of all staff lists
     * @throws StaffException if loading fails
     */
    public ArrayList<StaffList> getAllStaffLists() throws StaffException {
        return StaffList.loadAllStaffLists();
    }
}