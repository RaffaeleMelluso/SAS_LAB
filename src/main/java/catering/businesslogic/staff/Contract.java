package catering.businesslogic.staff;
import java.util.Date;

public class Contract {
  
  private int contractId;
  private String engagement;
  private Date startDate;
  private Date endDate;
  private Employee employee;

  public Contract(int contractId, String engagement, Date startDate, Date endDate, Employee employee  ) {
    this.contractId = contractId;
    this.engagement = engagement;
    this.startDate = startDate;
    this.endDate = endDate;
  }
  public int getContractId() {
    return contractId;
  }
  public void setContractId(int contractId) {
    this.contractId = contractId;
  }
  public String getEngagement() {
    return engagement;
  }
  public void setEngagement(String engagement) {
    this.engagement = engagement;
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
