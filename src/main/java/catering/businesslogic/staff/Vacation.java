package catering.businesslogic.staff;
import java.util.Date;

public class Vacation {
  private String state;
  private Date startDate;
  private Date endDate;
  private Employee currentEmployee;

  public Vacation(String state, Date startDate, Date endDate, Employee currentEmployee) {
    this.state = state;
    this.startDate = startDate;
    this.endDate = endDate;
  }
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }
  public Date getStartDate() {
    return startDate;
  }
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }
  public Date getEndDate() {
    return endDate;
  }
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }
}
