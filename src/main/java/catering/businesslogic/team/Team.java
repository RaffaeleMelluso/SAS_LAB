package catering.businesslogic.team;
import catering.businesslogic.staff.Employee;
import java.util.Map;

public class Team {

    private String name;
    private int staffToAssign;
    private Map<Employee,String> team;

    public Team(String name, Map<Employee, String> team, int staffToAssign) {
        this.staffToAssign = staffToAssign;
        this.team = team;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStaffToAssign() {
        return staffToAssign;
    }
    public void setStaffToAssign(int staffToAssign) {
        this.staffToAssign = staffToAssign;
    }
    public Map<Employee, String> getTeam() {
        return team;
    }
    public void setTeam(Map<Employee, String> team) {
        this.team = team;
    }
    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", staffToAssign=" + staffToAssign +
                ", team=" + team +
                '}';
    }
    

}
