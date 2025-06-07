package catering.businesslogic.kitchen;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.employee.Employee;
import catering.businesslogic.staff.Contract;
import catering.businesslogic.staff.StaffException;
import catering.businesslogic.staff.StaffList;
import catering.businesslogic.staff.StaffManager;
import catering.businesslogic.staff.Vacation;
import catering.businesslogic.team.Team;
import catering.businesslogic.team.TeamException;
import catering.businesslogic.team.TeamManager;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
public class staffManagementTest {

    private static final Logger LOGGER = LogManager.getLogger(staffManagementTest.class);

    private static CatERing app;
    private static User owner;
    private static User organizer;
    private static Employee testEmployee1;
    private static Employee testEmployee2;
    private static StaffList testStaffList;
    private static Team testTeam;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    @BeforeAll
    static void cleanDatabase() {
        // Elimina il file database per garantire un ambiente pulito
        File dbFile = new File("database/catering.db");
        if (dbFile.exists()) {
            dbFile.delete();
            LOGGER.info("Database file deleted for clean test environment");
        }
    }

    @BeforeEach
    void setup() {
        try {
            // Setup owner user
            owner = User.load("Mario Rossi");
            assertNotNull(owner, "Owner user should be loaded");
            assertTrue(owner.isOwner(), "User should have owner role");

            // Setup organizer user
            organizer = User.load("Luigi Verdi");
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");

            // Create test employees
            testEmployee1 = new Employee("Test Employee 1", "TSTMPL80A01H501U");
            testEmployee1.save();
            
            testEmployee2 = new Employee("Test Employee 2", "TSTMPL85A01H501X");
            testEmployee2.save();

            LOGGER.info("Setup completed successfully");

        } catch (Exception e) {
            LOGGER.severe("Setup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testOwnerCreatesStaffList() {
        LOGGER.info("Testing owner creates staff list");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());
            assertEquals(owner, app.getUserManager().getCurrentUser(), "Current user should be the owner");

            // Create staff list
            StaffManager staffManager = app.getStaffManager();
            testStaffList = staffManager.createStaffList("Test Staff List", owner);

            // Verify staff list creation
            assertNotNull(testStaffList, "Staff list should not be null");
            assertEquals("Test Staff List", testStaffList.getName(), "Staff list name should match");
            assertEquals(owner, testStaffList.getOwner(), "Staff list owner should be correct");

            LOGGER.info("Successfully created staff list: " + testStaffList.toString());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testOwnerCreatesContracts() {
        LOGGER.info("Testing owner creates contracts");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Create permanent contract
            Date startDate1 = Date.valueOf("2025-01-01");
            Contract permanentContract = staffManager.createContract("permanente", startDate1, null, testEmployee1);

            assertNotNull(permanentContract, "Permanent contract should not be null");
            assertEquals("permanente", permanentContract.getEngagement(), "Contract engagement should be permanent");
            assertEquals(testEmployee1, permanentContract.getEmployee(), "Contract employee should match");

            // Create occasional contract
            Date startDate2 = Date.valueOf("2025-06-01");
            Date endDate2 = Date.valueOf("2025-12-31");
            Contract occasionalContract = staffManager.createContract("occasionale", startDate2, endDate2, testEmployee2);

            assertNotNull(occasionalContract, "Occasional contract should not be null");
            assertEquals("occasionale", occasionalContract.getEngagement(), "Contract engagement should be occasional");
            assertEquals(testEmployee2, occasionalContract.getEmployee(), "Contract employee should match");

            LOGGER.info("Successfully created contracts for employees");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testOwnerModifiesContractEngagement() {
        LOGGER.info("Testing owner modifies contract engagement");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Create occasional contract first
            Date startDate = Date.valueOf("2025-06-01");
            Date endDate = Date.valueOf("2025-12-31");
            Contract contract = staffManager.createContract("occasionale", startDate, endDate, testEmployee2);

            assertEquals("occasionale", contract.getEngagement(), "Contract should initially be occasional");

            // Change to permanent
            staffManager.changeContractToPermanent(contract);

            assertEquals("permanente", contract.getEngagement(), "Contract engagement should be changed to permanent");

            LOGGER.info("Successfully changed contract engagement to permanent");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void testOwnerManagesEmployeesInStaffList() {
        LOGGER.info("Testing owner manages employees in staff list");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Create staff list if not exists
            if (testStaffList == null) {
                testStaffList = staffManager.createStaffList("Test Staff List", owner);
            }

            // Add employees to staff list
            staffManager.addEmployeeToStaffList(testEmployee1, "permanente");
            staffManager.addEmployeeToStaffList(testEmployee2, "occasionale");

            // Verify employees are added
            assertTrue(testStaffList.hasEmployee(testEmployee1), "Staff list should contain employee 1");
            assertTrue(testStaffList.hasEmployee(testEmployee2), "Staff list should contain employee 2");
            assertEquals("permanente", testStaffList.getEngagementType(testEmployee1), "Employee 1 should be permanent");
            assertEquals("occasionale", testStaffList.getEngagementType(testEmployee2), "Employee 2 should be occasional");

            // Remove employee from staff list
            staffManager.removeEmployeeFromStaffList(testEmployee2);
            assertFalse(testStaffList.hasEmployee(testEmployee2), "Employee 2 should be removed from staff list");

            LOGGER.info("Successfully managed employees in staff list");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void testOwnerEvaluatesVacationRequests() {
        LOGGER.info("Testing owner evaluates vacation requests");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Create vacation request
            Date vacationStart = Date.valueOf("2025-07-01");
            Date vacationEnd = Date.valueOf("2025-07-15");
            Vacation vacation = new Vacation(testEmployee1, vacationStart, vacationEnd);
            vacation.save();

            assertEquals("Non valutata", vacation.getState(), "Vacation should initially be not evaluated");

            // Evaluate vacation request - approve
            staffManager.evaluateVacationRequest(vacation, "Accettata");
            assertEquals("Accettata", vacation.getState(), "Vacation should be approved");

            // Create another vacation request and reject it
            Date vacationStart2 = Date.valueOf("2025-08-01");
            Date vacationEnd2 = Date.valueOf("2025-08-10");
            Vacation vacation2 = new Vacation(testEmployee1, vacationStart2, vacationEnd2);
            vacation2.save();

            staffManager.evaluateVacationRequest(vacation2, "Rifiutata");
            assertEquals("Rifiutata", vacation2.getState(), "Vacation should be rejected");

            LOGGER.info("Successfully evaluated vacation requests");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testOwnerCreatesTeamAndAssignsPermanentEmployee() {
        LOGGER.info("Testing owner creates team and assigns permanent employee");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            TeamManager teamManager = app.getTeamManager();
            StaffManager staffManager = app.getStaffManager();

            // Create team
            testTeam = teamManager.createTeam("Test Team", 5);
            assertNotNull(testTeam, "Team should not be null");
            assertEquals("Test Team", testTeam.getName(), "Team name should match");
            assertEquals(5, testTeam.getStaffToAssign(), "Team should have 5 staff slots");

            // Set current team
            teamManager.setCurrentTeam(testTeam);

            // Ensure employee is in staff list as permanent
            if (testStaffList == null) {
                testStaffList = staffManager.createStaffList("Test Staff List", owner);
            }
            staffManager.addEmployeeToStaffList(testEmployee1, "permanente");

            // Assign permanent employee to team
            staffManager.assignPermanentEmployeeToTeam(testEmployee1, testTeam);

            // Verify assignment
            assertTrue(testTeam.getTeam().containsKey(testEmployee1), "Team should contain the permanent employee");
            assertEquals("permanente", testTeam.getTeam().get(testEmployee1), "Employee should be assigned as permanent");

            LOGGER.info("Successfully created team and assigned permanent employee");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    void testOrganizerAssignsOccasionalEmployeeToTeam() {
        LOGGER.info("Testing organizer assigns occasional employee to team");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), "Current user should be the organizer");

            StaffManager staffManager = app.getStaffManager();

            // Ensure staff list exists with occasional employee
            if (testStaffList == null) {
                // Switch to owner to create staff list
                app.getUserManager().fakeLogin(owner.getUserName());
                testStaffList = staffManager.createStaffList("Test Staff List", owner);
                staffManager.addEmployeeToStaffList(testEmployee2, "occasionale");
                // Switch back to organizer
                app.getUserManager().fakeLogin(organizer.getUserName());
            } else {
                // Switch to owner to add employee
                app.getUserManager().fakeLogin(owner.getUserName());
                staffManager.addEmployeeToStaffList(testEmployee2, "occasionale");
                // Switch back to organizer
                app.getUserManager().fakeLogin(organizer.getUserName());
            }

            // Ensure team exists
            if (testTeam == null) {
                // Switch to owner to create team
                app.getUserManager().fakeLogin(owner.getUserName());
                testTeam = app.getTeamManager().createTeam("Test Team", 5);
                // Switch back to organizer
                app.getUserManager().fakeLogin(organizer.getUserName());
            }

            // Assign occasional employee to team (organizer can do this)
            staffManager.assignOccasionalEmployeeToTeam(testEmployee2, testTeam);

            LOGGER.info("Successfully assigned occasional employee to team as organizer");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    void testOrganizerCannotCreateStaffList() {
        LOGGER.info("Testing organizer cannot create staff list");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Try to create staff list - should fail
            StaffException exception = assertThrows(StaffException.class, () -> {
                staffManager.createStaffList("Unauthorized List", organizer);
            });

            assertTrue(exception.getMessage().contains("Only owners can create staff lists"), 
                      "Exception message should indicate permission denied");

            LOGGER.info("Correctly prevented organizer from creating staff list");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Order(9)
    void testOrganizerCannotCreateContracts() {
        LOGGER.info("Testing organizer cannot create contracts");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Try to create contract - should fail
            Date startDate = Date.valueOf("2025-01-01");
            StaffException exception = assertThrows(StaffException.class, () -> {
                staffManager.createContract("permanente", startDate, null, testEmployee1);
            });

            assertTrue(exception.getMessage().contains("Only owners can create contracts"), 
                      "Exception message should indicate permission denied");

            LOGGER.info("Correctly prevented organizer from creating contracts");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    void testOrganizerCannotEvaluateVacations() {
        LOGGER.info("Testing organizer cannot evaluate vacation requests");

        try {
            // Login as organizer
            app.getUserManager().fakeLogin(organizer.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Create vacation request
            Date vacationStart = Date.valueOf("2025-09-01");
            Date vacationEnd = Date.valueOf("2025-09-10");
            Vacation vacation = new Vacation(testEmployee1, vacationStart, vacationEnd);
            vacation.save();

            // Try to evaluate vacation - should fail
            StaffException exception = assertThrows(StaffException.class, () -> {
                staffManager.evaluateVacationRequest(vacation, "Accettata");
            });

            assertTrue(exception.getMessage().contains("Only owners can evaluate vacation requests"), 
                      "Exception message should indicate permission denied");

            LOGGER.info("Correctly prevented organizer from evaluating vacation requests");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    void testOwnerDeletesContract() {
        LOGGER.info("Testing owner deletes contract");

        try {
            // Login as owner
            app.getUserManager().fakeLogin(owner.getUserName());

            StaffManager staffManager = app.getStaffManager();

            // Create contract to delete
            Date startDate = Date.valueOf("2025-01-01");
            Contract contract = staffManager.createContract("occasionale", startDate, Date.valueOf("2025-12-31"), testEmployee1);
            
            int contractId = contract.getContractId();
            assertTrue(contractId > 0, "Contract should have valid ID");

            // Delete contract
            boolean deleted = staffManager.deleteContract(contract);
            assertTrue(deleted, "Contract should be deleted successfully");

            LOGGER.info("Successfully deleted contract");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
