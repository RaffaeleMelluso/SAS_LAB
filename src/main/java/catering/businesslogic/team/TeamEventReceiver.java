package catering.businesslogic.team;

import catering.businesslogic.employee.Employee;

/**
 * Interface for receiving team-related notifications.
 * Implemented by classes that need to respond to team changes.
 */
public interface TeamEventReceiver {
    /**
     * Called when a new team is created.
     * @param team The created team
     */
    void onTeamCreated(Team team);

    /**
     * Called when a team is selected.
     * @param team The selected team
     */
    void onTeamSelected(Team team);

    /**
     * Called when an employee is added to a team.
     * @param team The team
     * @param employee The employee added
     * @param engagement The engagement type ("permanente" or "occasionale")
     */
    void onEmployeeAdded(Team team, Employee employee, String engagement);
}