package catering.businesslogic.staff;

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

    private static StaffList instance = null;

    private int id;
    private String name;
    private Map<Employee, String> staff;
    private User owner;

    private StaffList() {
        this.staff = new HashMap<>();
    }

    private StaffList(String name, User owner) {
        this();
        this.name = name;
        this.owner = owner;
    }

    public static StaffList getInstance() {
        if (instance == null) {
            instance = loadFromDb();
        }
        return instance;
    }

    public static StaffList create(String name, User owner) throws StaffException {
        if (instance != null) throw new StaffException("Staff list already exists");
        instance = new StaffList(name, owner);
        instance.saveStaffList();
        return instance;
    }

    public void addEmployee(Employee employee, String engagementType) throws StaffException {
        if (employee == null) throw new StaffException("Employee cannot be null");
        staff.put(employee, engagementType);
        saveStaffMembers();
    }

    public boolean removeEmployee(Employee employee) throws StaffException {
        if (employee == null) throw new StaffException("Employee cannot be null");
        boolean removed = staff.remove(employee) != null;
        if (removed) saveStaffMembers();
        return removed;
    }

    public String getEngagementType(Employee employee) {
        return staff.get(employee);
    }

    public String getName() { return name; }
    public User getOwner() { return owner; }
    public int getId() { return id; }
    public Map<Employee, String> getStaff() { return new HashMap<>(staff); }

    public void saveStaffList() throws StaffException {
        if (id != 0) return; // Already saved
        String query = "INSERT INTO StaffLists (name, owner_id) VALUES (?, ?)";
        PersistenceManager.executeUpdate(query, name, owner.getId());
        this.id = PersistenceManager.getLastId();
        saveStaffMembers();
    }

    public void updateStaffList() throws StaffException {
        if (id == 0) throw new StaffException("Cannot update staff list that hasn't been saved");
        String query = "UPDATE StaffLists SET name = ?, owner_id = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, name, owner.getId(), id);
        saveStaffMembers();
    }

    private void saveStaffMembers() {
        if (id == 0) return;
        PersistenceManager.executeUpdate("DELETE FROM StaffAssociations WHERE stafflist_id = ?", id);
        for (Map.Entry<Employee, String> entry : staff.entrySet()) {
            PersistenceManager.executeUpdate(
                "INSERT INTO StaffAssociations (stafflist_id, employee_id, engagement) VALUES (?, ?, ?)",
                id, entry.getKey().getId(), entry.getValue()
            );
        }
    }

    private static StaffList loadFromDb() {
        final StaffList[] holder = new StaffList[1];
        PersistenceManager.executeQuery("SELECT * FROM StaffLists LIMIT 1", new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                StaffList sl = new StaffList();
                sl.id = rs.getInt("id");
                sl.name = rs.getString("name");
                sl.owner = User.load(rs.getInt("owner_id"));
                sl.loadStaffMembers();
                holder[0] = sl;
            }
        });
        return holder[0];
    }

    private void loadStaffMembers() {
        if (id == 0) return;
        staff.clear();
        PersistenceManager.executeQuery("SELECT employee_id, engagement FROM StaffAssociations WHERE stafflist_id = ?", new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                Employee e = Employee.loadById(rs.getInt("employee_id"));
                String engagement = rs.getString("engagement");
                if (e != null) staff.put(e, engagement);
            }
        }, id);
    }

    public boolean hasEmployee(Employee employee) {
        return staff.containsKey(employee);
    }
}