package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a staff list for managing employees and their assignments.
 * Each staff list has an owner and a map of employees with their engagement type.
 */
public class StaffList {
    private static final Logger LOGGER = LogManager.getLogger(StaffList.class);

    private int id;
    private String name;
    private Map<Employee, String> staff;
    private User owner;

    /**
     * Default constructor for creating an empty staff list.
     */
    public StaffList() {
        this.staff = new HashMap<>();
        LOGGER.info("Created new empty StaffList");
    }

    /**
     * Constructor for creating a staff list with an owner.
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
     * Constructor for creating a staff list with a name and owner.
     * @param name  The name of the staff list
     * @param owner The owner of the staff list
     */
    public StaffList(String name, User owner) {
        this(owner);
        this.name = name;
    }

    /**
     * Adds an employee to this staff list.
     * @param employee The employee to add
     * @param engagementType The type of engagement ("permanente" or "occasionale")
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
     * Removes an employee from this staff list.
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
     * Gets the engagement type of an employee in this staff list.
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
     * Saves this staff list to the database, including its members.
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
            saveStaffMembers();
            LOGGER.info("Successfully saved StaffList with ID: " + this.id);
        } catch (Exception e) {
            LOGGER.severe("Failed to save StaffList: " + e.getMessage());
            throw new StaffException("Failed to save staff list", e);
        }
    }

    /**
     * Updates this staff list in the database, including its members.
     * @throws StaffException if the update operation fails
     */
    public void updateStaffList() throws StaffException {
        if (id <= 0) {
            throw new StaffException("Cannot update staff list that hasn't been saved");
        }

        try {
            String query = "UPDATE StaffLists SET owner_id = ? WHERE id = ?";
            PersistenceManager.executeUpdate(query, owner.getId(), id);
            saveStaffMembers();
            LOGGER.info("Successfully updated StaffList with ID: " + this.id);
        } catch (Exception e) {
            LOGGER.severe("Failed to update StaffList: " + e.getMessage());
            throw new StaffException("Failed to update staff list", e);
        }
    }

    /**
     * Saves the staff members to the StaffAssociations table.
     */
    private void saveStaffMembers() {
        if (id == 0) return;
        // Remove all previous members
        PersistenceManager.executeUpdate("DELETE FROM StaffAssociations WHERE stafflist_id = ?", id);
        // Insert current members
        for (Map.Entry<Employee, String> entry : staff.entrySet()) {
            PersistenceManager.executeUpdate(
                "INSERT INTO StaffAssociations (stafflist_id, employee_id, engagement) VALUES (?, ?, ?)",
                id, entry.getKey().getId(), entry.getValue()
            );
        }
    }

    /**
     * Loads all staff lists from the database, including their members.
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
                    sl.loadStaffMembers();
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
     * Loads a staff list by its ID, including its members.
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
                    sl.loadStaffMembers();
                    staffListHolder[0] = sl;
                }
            }, id);

            LOGGER.info("Successfully loaded StaffList with ID: " + id);
        } catch (Exception e) {
            LOGGER.severe("Failed to load StaffList with ID " + id + ": " + e.getMessage());
            throw new StaffException("Failed to load staff list", e);
        }
        return staffListHolder[0];
    }

    /**
     * Loads the staff members for this staff list from the StaffAssociations table.
     */
    private void loadStaffMembers() {
        if (id == 0) return;
        String query = "SELECT employee_id, engagement FROM StaffAssociations WHERE stafflist_id = ?";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Employee e = Employee.loadById(rs.getInt("employee_id"));
                String engagement = rs.getString("engagement");
                if (e != null) {
                    staff.put(e, engagement);
                }
            }
        }, id);
    }

    private boolean isValidEngagementType(String engagementType) {
        return "permanente".equals(engagementType) || "occasionale".equals(engagementType);
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("ID cannot be negative");
        this.id = id;
    }

    public Map<Employee, String> getStaff() {
        return new HashMap<>(staff);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        if (owner == null) throw new IllegalArgumentException("Owner cannot be null");
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
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
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