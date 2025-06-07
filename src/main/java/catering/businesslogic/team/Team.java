package catering.businesslogic.team;

import catering.businesslogic.employee.Employee;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Team represents a group of employees for a specific purpose or event.
 * Each team has a name, a number of staff to assign, and a map of employees with their engagement type.
 */
public class Team {
    private static final Logger LOGGER = LogManager.getLogger(Team.class);

    private int id;
    private String name;
    private int staffToAssign;
    private Map<Employee, String> team; // Employee -> engagement ("permanente"/"occasionale")

    /**
     * Constructs a new Team with the given name, members, and staff to assign.
     * 
     * @param name           The name of the team
     * @param team           The map of employees and their engagement
     * @param staffToAssign  The number of staff to assign
     */
    public Team(String name, Map<Employee, String> team, int staffToAssign) {
        this.name = name;
        this.team = team;
        this.staffToAssign = staffToAssign;
    }

    /**
     * Default constructor for loading from DB.
     */
    public Team() {
        this("", new HashMap<>(), 0);
    }

    // ===== GETTERS & SETTERS =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getStaffToAssign() { return staffToAssign; }
    public void setStaffToAssign(int staffToAssign) { this.staffToAssign = staffToAssign; }
    public Map<Employee, String> getTeam() { return team; }
    public void setTeam(Map<Employee, String> team) { this.team = team; }

    // ===== PERSISTENCE METHODS =====

    /**
     * Loads a team by its ID from the database.
     * 
     * @param id The team ID
     * @return The loaded Team or null if not found
     */
    public static Team loadById(int id) {
        Team[] holder = new Team[1];
        String query = "SELECT * FROM Teams WHERE id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Team t = new Team();
                t.id = rs.getInt("id");
                t.name = rs.getString("name");
                t.staffToAssign = rs.getInt("staff_to_assign");
                t.team = loadTeamMembers(t.id);
                holder[0] = t;
            }
        }, id);
        return holder[0];
    }

    /**
     * Loads all members of a team from the database.
     * 
     * @param teamId The team ID
     * @return Map of Employee to engagement type
     */
    public static Map<Employee, String> loadTeamMembers(int teamId) {
        Map<Employee, String> members = new HashMap<>();
        String query = "SELECT employee_id, engagement FROM TeamMembers WHERE team_id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Employee e = Employee.loadById(rs.getInt("employee_id"));
                String engagement = rs.getString("engagement");
                if (e != null) {
                    members.put(e, engagement);
                }
            }
        }, teamId);
        return members;
    }

    /**
     * Saves this team to the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean save() {
        if (id != 0) return false; // Already exists
        String query = "INSERT INTO Teams (name, staff_to_assign) VALUES (?, ?)";
        PersistenceManager.executeUpdate(query, name, staffToAssign);
        id = PersistenceManager.getLastId();
        LOGGER.info("Created new team: " + name + " (ID: " + id + ")");
        saveTeamMembers();
        return id > 0;
    }

    /**
     * Updates this team in the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean update() {
        if (id == 0) return false; // Not in DB
        String query = "UPDATE Teams SET name = ?, staff_to_assign = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, name, staffToAssign, id);
        LOGGER.info("Updated team: " + name + " (ID: " + id + ")");
        saveTeamMembers();
        return rows > 0;
    }

    /**
     * Saves all team members to the database.
     */
    private void saveTeamMembers() {
        if (id == 0) return;
        // Remove all previous members
        PersistenceManager.executeUpdate("DELETE FROM TeamMembers WHERE team_id = ?", id);
        // Insert current members
        for (Map.Entry<Employee, String> entry : team.entrySet()) {
            PersistenceManager.executeUpdate(
                "INSERT INTO TeamMembers (team_id, employee_id, engagement) VALUES (?, ?, ?)",
                id, entry.getKey().getId(), entry.getValue()
            );
        }
    }

    /**
     * Deletes this team from the database.
     * 
     * @return true if successful, false otherwise
     */
    public boolean delete() {
        if (id == 0)
            return false; // Not in DB

        // Remove all team members first
        PersistenceManager.executeUpdate("DELETE FROM TeamMembers WHERE team_id = ?", id);

        // Then remove the team itself
        String deleteQuery = "DELETE FROM Teams WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(deleteQuery, id);

        if (rows > 0) {
            LOGGER.info("Deleted team: " + name + " (ID: " + id + ")");
            id = 0;
            return true;
        } else {
            LOGGER.warning("Failed to delete team: " + name + " (ID: " + id + ")");
            return false;
        }
    }

    /**
     * Determines if this team is equal to another object.
     * Two teams are considered equal if they have the same ID or, if ID is 0, the same name and staffToAssign.
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

        Team other = (Team) obj;

        // If both teams have valid IDs, compare by ID
        if (this.id > 0 && other.id > 0) {
            return this.id == other.id;
        }

        // Otherwise, compare by name and staffToAssign
        boolean nameMatch = (this.name == null && other.name == null) ||
                (this.name != null && this.name.equals(other.name));
        boolean staffMatch = this.staffToAssign == other.staffToAssign;

        return nameMatch && staffMatch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        if (id > 0) {
            result = prime * result + id;
        } else {
            result = prime * result + (name != null ? name.hashCode() : 0);
            result = prime * result + staffToAssign;
        }

        return result;
    }

    @Override
    public String toString() {
        return "Team [id=" + id + ", name='" + name + "', staffToAssign=" + staffToAssign + ", team=" + team + "]";
    }
}