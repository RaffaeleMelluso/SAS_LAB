package catering.businesslogic.employee;


public class Employee {
  private String name;
  private String fiscalCode;
  private String email;
  private String phoneNumber;
  private int vacationDaysLeft=0;

  private Employee() {
  }

  public Employee(String name, String fiscalCode, String email, String phoneNumber) {
    this.name = name;
    this.fiscalCode = fiscalCode;
    this.email = email;
    this.phoneNumber = phoneNumber;
  }

  public Employee(String name, String fiscalCode) {
    this.name = name;
    this.fiscalCode = fiscalCode;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getFiscalCode() {
    return fiscalCode;
  }

  public void setFiscalCode(String fiscalCode) {
    this.fiscalCode = fiscalCode;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public int getVacationDaysLeft() {
    return vacationDaysLeft;
  }

  public void setVacationDaysLeft(int vacationDaysLeft) {
    this.vacationDaysLeft = vacationDaysLeft;
  }

  


}

