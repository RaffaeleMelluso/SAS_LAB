package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.team.Team;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a staff list for managing employees and their assignments
 */
public class StaffList {
    private static final Logger LOGGER = LogManager.getLogger(StaffList.class);
    
    private int id;
    private String name;
    private Map<Employee, String> staff;
    private User owner;

    /**
     * Default constructor for creating an empty staff list
     */
    public StaffList() {
        this.staff = new HashMap<>();
        LOGGER.info("Created new empty StaffList");
    }

    /**
     * Constructor for creating a staff list with an owner
     * @param owner The owner of the staff list
     */
    public StaffList(User owner) {
        this();
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        this.owner = owner;
        LOGGER.info("Created new StaffList for owner: " + owner.getUserName());
    }

    /**
     * Adds an employee to this staff list
     * @param employee The employee to add
     * @param engagementType The type of engagement (e.g., permanent or occasional)
     * @throws StaffException if the employee cannot be added
     */
    public void addEmployee(Employee employee, String engagementType) throws StaffException {
        LOGGER.info("Attempting to add employee with engagement type: " + engagementType);
        
        if (employee == null) {
            LOGGER.warning("Attempted to add null employee");
            throw new StaffException("Employee cannot be null");
        }
        
        if (engagementType == null || engagementType.trim().isEmpty()) {
            LOGGER.warning("Invalid engagement type provided: " + engagementType);
            throw new StaffException("Engagement type cannot be null or empty");
        }
        
        if (!isValidEngagementType(engagementType)) {
            LOGGER.warning("Invalid engagement type: " + engagementType);
            throw new StaffException("Invalid engagement type. Must be 'permanente' or 'occasionale'");
        }
        
        if (staff.containsKey(employee)) {
            LOGGER.warning("Employee already exists in staff list: " + employee.getName());
            throw new StaffException("Employee is already in the staff list");
        }
        
        staff.put(employee, engagementType);
        LOGGER.info("Successfully added employee: " + employee.getName() + " with type: " + engagementType);
    }

    /**
     * Removes an employee from this staff list
     * @param employee The employee to remove
     * @return true if removed successfully, false if not found
     * @throws StaffException if the employee cannot be removed
     */
    public boolean removeEmployee(Employee employee) throws StaffException {
        if (employee == null) {
            LOGGER.warning("Attempted to remove null employee");
            throw new StaffException("Employee cannot be null");
        }
        
        boolean removed = staff.remove(employee) != null;
        if (removed) {
            LOGGER.info("Successfully removed employee: " + employee.getName());
        } else {
            LOGGER.warning("Employee not found in staff list: " + employee.getName());
        }
        
        return removed;
    }

    /**
     * Gets the engagement type of an employee in this staff list
     * @param employee The employee whose engagement type to retrieve
     * @return The engagement type, or null if the employee is not in the staff list
     */
    public String getEngagementType(Employee employee) {
        if (employee == null) {
            LOGGER.warning("Attempted to get engagement type for null employee");
            return null;
        }
        
        String engagementType = staff.get(employee);
        LOGGER.fine("Retrieved engagement type for " + employee.getName() + ": " + engagementType);
        return engagementType;
    }

    /**
     * Assigns a permanent employee to a team
     * @param employee The employee to assign
     * @param team The team to assign the employee to
     * @return true if the assignment was successful, false otherwise
     * @throws StaffException if the assignment fails
     */
    public boolean assignPermanentEmployee(Employee employee, Team team) throws StaffException {
        LOGGER.info("Attempting to assign permanent employee to team");
        
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        
        if (team == null) {
            throw new StaffException("Team cannot be null");
        }
        
        String engagementType = getEngagementType(employee);
        if (engagementType == null) {
            LOGGER.warning("Employee not found in staff list: " + employee.getName());
            throw new StaffException("Employee is not in this staff list");
        }
        
        if (!"permanente".equals(engagementType)) {
            LOGGER.warning("Attempted to assign non-permanent employee: " + employee.getName());
            throw new StaffException("Only permanent employees can be assigned to teams");
        }
        
        team.addEmployee(employee, engagementType);
        LOGGER.info("Successfully assigned permanent employee " + employee.getName() + " to team " + team.getName());
        return true;
    }

    /**
     * Saves this staff list to the database
     * @throws StaffException if the save operation fails
     */
    public void saveStaffList() throws StaffException {
        if (owner == null) {
            throw new StaffException("Cannot save staff list without an owner");
        }
        
        try {
            String query = "INSERT INTO StaffLists (owner_id) VALUES (?)";
            PersistenceManager.executeUpdate(query, owner.getId());
            this.id = PersistenceManager.getLastId();
            LOGGER.info("Successfully saved StaffList with ID: " + this.id);
        } catch (Exception e) {
            LOGGER.severe("Failed to save StaffList: " + e.getMessage());
            throw new StaffException("Failed to save staff list", e);
        }
    }

    /**
     * Updates this staff list in the database
     * @throws StaffException if the update operation fails
     */
    public void updateStaffList() throws StaffException {
        if (id <= 0) {
            throw new StaffException("Cannot update staff list that hasn't been saved");
        }
        
        try {
            String query = "UPDATE StaffLists SET owner_id = ? WHERE id = ?";
            PersistenceManager.executeUpdate(query, owner.getId(), id);
            LOGGER.info("Successfully updated StaffList with ID: " + this.id);
        } catch (Exception e) {
            LOGGER.severe("Failed to update StaffList: " + e.getMessage());
            throw new StaffException("Failed to update staff list", e);
        }
    }

    /**
     * Loads all staff lists from the database
     * @return List of all staff lists
     * @throws StaffException if the load operation fails
     */
    public static ArrayList<StaffList> loadAllStaffLists() throws StaffException {
        ArrayList<StaffList> staffLists = new ArrayList<>();
        
        try {
            String query = "SELECT * FROM StaffLists";
            
            PersistenceManager.executeQuery(query, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    StaffList sl = new StaffList();
                    sl.id = rs.getInt("id");
                    sl.owner = User.load(rs.getInt("owner_id"));
                    staffLists.add(sl);
                }
            });
            
            LOGGER.info("Successfully loaded " + staffLists.size() + " staff lists");
        } catch (Exception e) {
            LOGGER.severe("Failed to load staff lists: " + e.getMessage());
            throw new StaffException("Failed to load staff lists", e);
        }
        
        return staffLists;
    }

    /**
     * Loads a staff list by its ID
     * @param id The ID of the staff list to load
     * @return The staff list with the given ID, or null if not found
     * @throws StaffException if the load operation fails
     */
    public static StaffList loadById(int id) throws StaffException {
        if (id <= 0) {
            throw new StaffException("Invalid ID: " + id);
        }
        
        StaffList[] staffListHolder = new StaffList[1];
        
        try {
            String query = "SELECT * FROM StaffLists WHERE id = ?";
            
            PersistenceManager.executeQuery(query, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    StaffList sl = new StaffList();
                    sl.id = rs.getInt("id");
                    sl.owner = User.load(rs.getInt("owner_id"));
                    staffListHolder[0] = sl;
                }
            }, id);
            
            if (staffListHolder[0] != null) {
                LOGGER.info("Successfully loaded StaffList with ID: " + id);
            } else {
                LOGGER.warning("StaffList not found with ID: " + id);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to load StaffList with ID " + id + ": " + e.getMessage());
            throw new StaffException("Failed to load staff list", e);
        }
        
        return staffListHolder[0];
    }

    private boolean isValidEngagementType(String engagementType) {
        return "permanente".equals(engagementType) || "occasionale".equals(engagementType);
    }

    // Getters e setters con validazione
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        this.id = id;
    }

    public Map<Employee, String> getStaff() {
        return new HashMap<>(staff);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        this.owner = owner;
    }

    public int getStaffCount() {
        return staff.size();
    }

    public boolean hasEmployee(Employee employee) {
        return staff.containsKey(employee);
    }

    public boolean isEmpty() {
        return staff.isEmpty();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StaffList staffList = (StaffList) obj;
        return id == staffList.id;
    }

    @Override
    public String toString() {
        return "StaffList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", staffCount=" + staff.size() +
                ", owner=" + (owner != null ? owner.getUserName() : "null") +
                '}';
    }
}
