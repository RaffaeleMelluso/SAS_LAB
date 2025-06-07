package catering.businesslogic.staff;

import java.util.Date;
import java.util.logging.Logger;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.persistence.ResultHandler;
import catering.util.LogManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Contract {
    private static final Logger LOGGER = LogManager.getLogger(Contract.class);
    
    private int contractId;
    private String engagement;
    private Date startDate;
    private Date endDate;
    private Employee employee;
    private User owner;

    public Contract() {
        LOGGER.fine("Created new empty Contract");
    }

    public Contract(String engagement, Date startDate, Date endDate, Employee employee) throws StaffException {
        // Validation first
        if (engagement == null || engagement.trim().isEmpty()) {
            throw new StaffException("Engagement type cannot be null or empty");
        }
        if (!isValidEngagementType(engagement)) {
            throw new StaffException("Invalid engagement type: " + engagement);
        }
        if (startDate == null) {
            throw new StaffException("Start date cannot be null");
        }
        if (endDate != null && startDate.after(endDate)) {
            throw new StaffException("Start date cannot be after end date");
        }
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        
        // Set fields after validation
        this.contractId = 0; // Will be set by database auto-increment
        this.engagement = engagement.trim();
        this.startDate = startDate;
        this.endDate = endDate;
        this.employee = employee;
        
        LOGGER.info("Created contract for employee: " + employee.getName() + 
                   " with engagement: " + engagement);
    }

    private boolean isValidEngagementType(String engagement) {
        return "permanente".equals(engagement) || "occasionale".equals(engagement);
    }

    // Metodi di persistenza
    public void saveContract() throws StaffException {
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        
        if (contractId != 0) {
            // Contract already exists, update instead
            updateContract();
            return;
        }
        
        try {
            String query = "INSERT INTO Contracts (engagement, start_date, end_date, employee_id) VALUES (?, ?, ?, ?)";
            PersistenceManager.executeUpdate(query, 
                engagement, 
                startDate.getTime(), 
                endDate != null ? endDate.getTime() : null, 
                employee.getId());
            
            // Get the auto-generated ID
            this.contractId = PersistenceManager.getLastId();
            
            LOGGER.info("Saved contract with auto-generated ID: " + contractId);
        } catch (Exception e) {
            LOGGER.severe("Failed to save contract: " + e.getMessage());
            throw new StaffException("Failed to save contract: " + e.getMessage(), e);
        }
    }

    public void updateContract() throws StaffException {
        if (contractId <= 0) {
            throw new StaffException("Cannot update contract that hasn't been saved");
        }
        
        try {
            String query = "UPDATE Contracts SET engagement = ?, start_date = ?, end_date = ?, employee_id = ? WHERE id = ?";
            Long startTimestamp = (startDate != null) ? startDate.getTime() : null;
            Long endTimestamp = (endDate != null) ? endDate.getTime() : null;
            int employeeId = (employee != null) ? employee.getId() : 0;
            
            PersistenceManager.executeUpdate(query, engagement, startTimestamp, endTimestamp, employeeId, contractId);
            LOGGER.info("Successfully updated contract with ID: " + this.contractId);
        } catch (Exception e) {
            LOGGER.severe("Failed to update contract: " + e.getMessage());
            throw new StaffException("Failed to update contract", e);
        }
    }

    public boolean deleteContract() throws StaffException {
        if (contractId <= 0) {
            throw new StaffException("Cannot delete contract that hasn't been saved");
        }
        
        try {
            String query = "DELETE FROM Contracts WHERE id = ?";
            boolean success = PersistenceManager.executeUpdate(query, contractId) > 0;
            
            if (success) {
                LOGGER.info("Successfully deleted contract with ID: " + this.contractId);
            } else {
                LOGGER.warning("Contract not found for deletion, ID: " + this.contractId);
            }
            
            return success;
        } catch (Exception e) {
            LOGGER.severe("Failed to delete contract: " + e.getMessage());
            throw new StaffException("Failed to delete contract", e);
        }
    }

    public static ArrayList<Contract> loadAllContracts() throws StaffException {
        ArrayList<Contract> contracts = new ArrayList<>();
        
        try {
            String query = "SELECT * FROM Contracts";
            
            PersistenceManager.executeQuery(query, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    Contract contract = new Contract();
                    contract.contractId = rs.getInt("id");
                    contract.engagement = rs.getString("engagement");
                    
                    long startTimestamp = rs.getLong("start_date");
                    if (!rs.wasNull()) {
                        contract.startDate = new Date(startTimestamp);
                    }
                    
                    long endTimestamp = rs.getLong("end_date");
                    if (!rs.wasNull()) {
                        contract.endDate = new Date(endTimestamp);
                    }
                    
                    int employeeId = rs.getInt("employee_id");
                    if (!rs.wasNull()) {
                        contract.employee = Employee.loadById(employeeId);
                    }
                    
                    contracts.add(contract);
                }
            });
            
            LOGGER.info("Successfully loaded " + contracts.size() + " contracts");
        } catch (Exception e) {
            LOGGER.severe("Failed to load contracts: " + e.getMessage());
            throw new StaffException("Failed to load contracts", e);
        }
        
        return contracts;
    }

    public static Contract loadById(int id) throws StaffException {
        if (id <= 0) {
            throw new StaffException("Invalid contract ID: " + id);
        }
        
        Contract[] contractHolder = new Contract[1];
        
        try {
            String query = "SELECT * FROM Contracts WHERE id = ?";
            
            PersistenceManager.executeQuery(query, new ResultHandler() {
                @Override
                public void handle(ResultSet rs) throws SQLException {
                    Contract contract = new Contract();
                    contract.contractId = rs.getInt("id");
                    contract.engagement = rs.getString("engagement");
                    
                    long startTimestamp = rs.getLong("start_date");
                    if (!rs.wasNull()) {
                        contract.startDate = new Date(startTimestamp);
                    }
                    
                    long endTimestamp = rs.getLong("end_date");
                    if (!rs.wasNull()) {
                        contract.endDate = new Date(endTimestamp);
                    }
                    
                    int employeeId = rs.getInt("employee_id");
                    if (!rs.wasNull()) {
                        contract.employee = Employee.loadById(employeeId);
                    }
                    
                    contractHolder[0] = contract;
                }
            }, id);
            
            if (contractHolder[0] != null) {
                LOGGER.info("Successfully loaded contract with ID: " + id);
            } else {
                LOGGER.warning("Contract not found with ID: " + id);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to load contract with ID " + id + ": " + e.getMessage());
            throw new StaffException("Failed to load contract", e);
        }
        
        return contractHolder[0];
    }

    // Utility methods
    public boolean isActive() {
        Date now = new Date();
        return startDate.before(now) && (endDate == null || endDate.after(now));
    }

    public int getDurationInDays() {
        if (startDate == null || endDate == null) {
            return -1; // Indefinite duration
        }
        long diffInMillis = endDate.getTime() - startDate.getTime();
        return (int) (diffInMillis / (1000 * 60 * 60 * 24)) + 1;
    }

    // Getters and setters with validation
    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        if (contractId < 0) {
            throw new IllegalArgumentException("Contract ID cannot be negative");
        }
        this.contractId = contractId;
    }

    public String getEngagement() {
        return engagement;
    }

    public void setEngagement(String engagement) throws StaffException {
        if (engagement == null || engagement.trim().isEmpty()) {
            throw new StaffException("Engagement type cannot be null or empty");
        }
        if (!isValidEngagementType(engagement)) {
            throw new StaffException("Invalid engagement type: " + engagement);
        }
        this.engagement = engagement.trim();
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) throws StaffException {
        if (startDate == null) {
            throw new StaffException("Start date cannot be null");
        }
        if (endDate != null && startDate.after(endDate)) {
            throw new StaffException("Start date cannot be after end date");
        }
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) throws StaffException {
        if (endDate != null && startDate != null && endDate.before(startDate)) {
            throw new StaffException("End date cannot be before start date");
        }
        this.endDate = endDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) throws StaffException {
        if (employee == null) {
            throw new StaffException("Employee cannot be null");
        }
        this.employee = employee;
    }

    /**
     * Changes engagement from occasional to permanent
     */
    public void changeEngagementToPermanent() throws StaffException {
        if (!"occasionale".equals(this.engagement)) {
            throw new StaffException("Contract is not occasional, cannot change to permanent");
        }
        this.engagement = "permanente";
        updateContract();
        LOGGER.info("Changed contract engagement to permanent for employee: " + employee.getName());
    }

    @Override
    public String toString() {
        return "Contract{" +
                "contractId=" + contractId +
                ", engagement='" + engagement + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", duration=" + getDurationInDays() + " days" +
                ", employee=" + (employee != null ? employee.getName() : "null") +
                ", active=" + isActive() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contract contract = (Contract) obj;
        return contractId == contract.contractId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(contractId);
    }
}
