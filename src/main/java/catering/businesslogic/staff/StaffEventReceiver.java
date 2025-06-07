package catering.businesslogic.staff;

import catering.businesslogic.employee.Employee;
import catering.businesslogic.team.Team;

public interface StaffEventReceiver {
    
    void updateStaffListAdded(StaffList staffList);
    
    void updateStaffListCreated(StaffList staffList);
    
    void updateContractDeleted(Contract contract);
    
    void updatePermanentEmployeeAdded(Employee employee, Team team);
    
    void updateEvaluateVacationRequest(Vacation vacation);
    
    void updateEmployeeDataModified(Employee employee);
    
    void updateContractCreated(Contract contract);
    
    void updateContractEngagementChanged(Contract contract);
    
    void updateEmployeeRemovedFromStaffList(Employee employee, StaffList staffList);
    
    void updateOccasionalEmployeeAssignedToTeam(Employee employee, Team team);
}
