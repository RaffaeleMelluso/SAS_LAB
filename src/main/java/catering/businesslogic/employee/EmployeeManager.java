package catering.businesslogic.employee;
import java.util.*;
import java.util.logging.Logger;


import catering.util.LogManager;

public class EmployeeManager {
  private static final Logger LOGGER = LogManager.getLogger(EmployeeManager.class);

  private Employee currentEmployee;

  public Employee getCurrentEmployee() {
        return this.currentEmployee;
    }

  public void updateVacationDaysLeft(Date start_date, Date end_date) {
    LOGGER.info("Updating vacation days left for employee: " + currentEmployee.getName());
    Employee currentEmployee = getCurrentEmployee();
    if (currentEmployee != null) {
      int vacationDays = (int) ((end_date.getTime() - start_date.getTime()) / (1000 * 60 * 60 * 24) + 1); // Assuming inclusive dates
      currentEmployee.setVacationDaysLeft(currentEmployee.getVacationDaysLeft() - vacationDays);

    }
  }

  public void modifyEmployeeData(String name, String fiscalCode, String email, String phoneNumber) {
    LOGGER.info("Modifying employee data for: " + name);
    Employee currentEmployee = getCurrentEmployee();
    if (currentEmployee != null) {
      currentEmployee.setName(name);
      currentEmployee.setFiscalCode(fiscalCode);
      currentEmployee.setEmail(email);
      currentEmployee.setPhoneNumber(phoneNumber);
    }
  }




}
