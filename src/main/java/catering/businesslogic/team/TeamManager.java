package catering.businesslogic.team;
import java.util.ArrayList;
import catering.businesslogic.team.TeamEventReceiver;
import catering.businesslogic.team.Team;

public class TeamManager {
  
  private Team currentTeam;
  private ArrayList<TeamEventReceiver> eventReceivers;

  public TeamManager() {
    this.eventReceivers = new ArrayList<>();
  }

  public void addEventReceiver(TeamEventReceiver receiver) {
    eventReceivers.add(receiver);
  }

  public Team getCurrentTeam() {
    return currentTeam;
  }

  addEmployee(Employee employee, String engagement) {
    if (currentTeam == null) {
      throw new TeamException("No current team set.");
    }
    if (currentTeam.getStaffToAssign() <= 0) {
      throw new TeamException("No staff slots available in the current team.");
    }
    
    currentTeam.getTeam().put(employee, engagement);
    currentTeam.setStaffToAssign(currentTeam.getStaffToAssign() - 1);
    
    for (TeamEventReceiver receiver : eventReceivers) {
      receiver.onEmployeeAdded(currentTeam, employee, engagement);
    }
  }

}
