package catering.businesslogic.team;

import catering.businesslogic.employee.Employee;
import catering.util.LogManager;
import catering.businesslogic.staff.StaffList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import catering.businesslogic.CatERing;
import catering.businesslogic.user.User;
/**
 * TeamManager handles all operations related to teams in the CatERing system.
 * It manages team creation, modification, and employee assignments.
 * It also notifies registered receivers of relevant events.
 */
public class TeamManager {
    private static final Logger LOGGER = LogManager.getLogger(TeamManager.class);

    private Team currentTeam;
    private final ArrayList<TeamEventReceiver> eventReceivers;

    /**
     * Constructor initializes the event receivers list.
     */
    public TeamManager() {
        this.eventReceivers = new ArrayList<>();
    }

    /**
     * Adds a team event receiver to be notified of team changes.
     * 
     * @param receiver The event receiver to add
     */
    public void addEventReceiver(TeamEventReceiver receiver) {
        if (receiver != null && !eventReceivers.contains(receiver)) {
            eventReceivers.add(receiver);
        }
    }

    /**
     * Removes a team event receiver.
     * 
     * @param receiver The event receiver to remove
     */
    public void removeEventReceiver(TeamEventReceiver receiver) {
        eventReceivers.remove(receiver);
    }

    /**
     * Gets the current team.
     * 
     * @return The current team or null if none selected
     */
    public Team getCurrentTeam() {
        return currentTeam;
    }

    /**
     * Sets the current team and notifies receivers.
     * 
     * @param team The team to set as current
     */
    public void setCurrentTeam(Team team) {
        this.currentTeam = team;
        LOGGER.info("Current team set: " + team);
        notifyTeamSelected(team);
    }

    /**
     * Creates a new team. Only owners can create teams.
     * 
     * @param name          The team name
     * @param staffToAssign Number of staff to assign
     * @return The newly created team
     * @throws TeamException if creation fails
     */
    public Team createTeam(String name, int staffToAssign) throws TeamException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new TeamException("Only owners can create teams");
        }
        Team team = new Team(name, new HashMap<>(), staffToAssign);
        if (team.save()) {
            this.currentTeam = team;
            LOGGER.info("Team created: " + team);
            notifyTeamCreated(team);
            return team;
        } else {
            LOGGER.warning("Failed to create team: " + name);
            throw new TeamException("Failed to create team");
        }
    }

    /**
     * Adds an employee to the current team with the specified engagement type.
     * 
     * @param employee   The employee to add
     * @param engagement The engagement type ("permanente" or "occasionale")
     * @throws TeamException if no team is selected, no slots available, or employee already present
     */
    public void addEmployee(Employee employee, String engagement) throws TeamException {
        if (currentTeam == null) {
            LOGGER.warning("No current team set.");
            throw new TeamException("No current team set.");
        }
        if (currentTeam.getStaffToAssign() <= 0) {
            LOGGER.warning("No staff slots available in the current team.");
            throw new TeamException("No staff slots available in the current team.");
        }
        if (currentTeam.getTeam().containsKey(employee)) {
            LOGGER.warning("Employee already in team: " + employee);
            throw new TeamException("Employee already in team.");
        }

        currentTeam.getTeam().put(employee, engagement);
        currentTeam.setStaffToAssign(currentTeam.getStaffToAssign() - 1);
        LOGGER.info("Added employee " + employee + " as " + engagement + " to team " + currentTeam.getName());

        notifyEmployeeAdded(currentTeam, employee, engagement);
        currentTeam.update(); // Persist change
    }

    /**
     * Assigns permanent employee from staff list to team. Only owners can do this.
     * 
     * @param employee The employee to assign
     * @param team     The team to assign the employee to
     * @param staffList The staff list
     * @throws TeamException if assignment fails
     */
    public void assignPermanentEmployeeFromStaffList(Employee employee, Team team, StaffList staffList) throws TeamException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOwner()) {
            throw new TeamException("Only owners can assign permanent employees to teams");
        }
        // implementation
    }

    /**
     * Assigns occasional employee from staff list to team. Organizers and owners can do this.
     * 
     * @param employee The employee to assign
     * @param team     The team to assign the employee to
     * @param staffList The staff list
     * @throws TeamException if assignment fails
     */
    public void assignOccasionalEmployeeFromStaffList(Employee employee, Team team, StaffList staffList) throws TeamException {
        User currentUser = CatERing.getInstance().getUserManager().getCurrentUser();
        if (!currentUser.isOrganizer() && !currentUser.isOwner()) {
            throw new TeamException("Only organizers and owners can assign occasional employees to teams");
        }
        // implementation
    }

    // ===== Event notification methods =====

    /**
     * Notifies receivers that a team has been created.
     * 
     * @param team The created team
     */
    private void notifyTeamCreated(Team team) {
        for (TeamEventReceiver receiver : eventReceivers) {
            receiver.onTeamCreated(team);
        }
    }

    /**
     * Notifies receivers that a team has been selected.
     * 
     * @param team The selected team
     */
    private void notifyTeamSelected(Team team) {
        for (TeamEventReceiver receiver : eventReceivers) {
            receiver.onTeamSelected(team);
        }
    }

    /**
     * Notifies receivers that an employee has been added to a team.
     * 
     * @param team      The team
     * @param employee  The employee added
     * @param engagement The engagement type
     */
    private void notifyEmployeeAdded(Team team, Employee employee, String engagement) {
        for (TeamEventReceiver receiver : eventReceivers) {
            receiver.onEmployeeAdded(team, employee, engagement);
        }
    }
}