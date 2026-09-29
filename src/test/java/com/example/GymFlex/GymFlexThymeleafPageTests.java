package com.example.GymFlex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.GymFlex.models.Member;
import com.example.GymFlex.models.Plan;
import com.example.GymFlex.repository.CheckInRepository;
import com.example.GymFlex.repository.MemberRepository;
import com.example.GymFlex.repository.MembershipRepository;
import com.example.GymFlex.repository.PlanRepository;
import com.example.GymFlex.services.MemberService;
import com.example.GymFlex.services.PlanService;

@SpringBootTest
public class GymFlexThymeleafPageTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PlanService planService;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        checkInRepository.deleteAll();
        membershipRepository.deleteAll();
        memberRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    @DisplayName("GET / returns login view when unauthenticated")
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("POST /login with valid credentials redirects to /dashboard")
    void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("POST /login with invalid credentials stays on login page with error")
    void testLoginFailure() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "admin")
                .param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @DisplayName("GET /dashboard redirects to /?error=unauthorized when unauthenticated")
    void testDashboardProtectionUnauthenticated() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?error=unauthorized"));
    }

    @Test
    @DisplayName("GET /dashboard returns dashboard view when authenticated")
    void testDashboardPageAuthenticated() throws Exception {
        mockMvc.perform(get("/dashboard")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("totalMembers", "activeMembers", "expiringSoonCount", "todayCheckIns"));
    }

    @Test
    @DisplayName("POST /logout invalidates session and redirects to /")
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("GET /members returns members view when authenticated")
    void testMembersPage() throws Exception {
        mockMvc.perform(get("/members")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("members"))
                .andExpect(model().attributeExists("members"));
    }

    @Test
    @DisplayName("GET /members/add returns add-member form view with plans")
    void testAddMemberPage() throws Exception {
        planService.createPlan(new Plan("Monthly", 1, 1000.0));
        planService.createPlan(new Plan("Quarterly", 3, 2500.0));
        planService.createPlan(new Plan("Yearly", 12, 9000.0));

        mockMvc.perform(get("/members/add")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-member"))
                .andExpect(model().attributeExists("registerRequest", "plans"));
    }

    @Test
    @DisplayName("POST /members/add submits form and redirects to member profile")
    void testSubmitAddMember() throws Exception {
        Plan plan = planService.createPlan(new Plan("Yearly", 12, 9000.0));

        mockMvc.perform(post("/members/add")
                .sessionAttr("loggedInUser", "Gym Admin")
                .param("name", "Sethu Kamal")
                .param("phone", "9876543210")
                .param("email", "sethu@gmail.com")
                .param("planId", String.valueOf(plan.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /members/{id} returns member-details view")
    void testMemberDetailsPage() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(get("/members/" + member.getId())
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("member-details"))
                .andExpect(model().attributeExists("member", "membership", "attendanceCount", "checkIns", "membershipStatus"));
    }

    @Test
    @DisplayName("POST /members/{id}/checkin records check-in and redirects")
    void testCheckInAction() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(post("/members/" + member.getId() + "/checkin")
                .sessionAttr("loggedInUser", "Gym Admin")
                .param("redirectUrl", "/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /members/{id}/renew renews plan and redirects")
    void testRenewAction() throws Exception {
        Plan plan = planService.createPlan(new Plan("Yearly", 12, 9000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(post("/members/" + member.getId() + "/renew")
                .sessionAttr("loggedInUser", "Gym Admin")
                .param("redirectUrl", "/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /plans returns plans view with all plans including Yearly")
    void testPlansPage() throws Exception {
        planService.createPlan(new Plan("Monthly", 1, 1000.0));
        planService.createPlan(new Plan("Quarterly", 3, 2500.0));
        planService.createPlan(new Plan("Yearly", 12, 9000.0));

        mockMvc.perform(get("/plans")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("plans"))
                .andExpect(model().attributeExists("plans"));
    }

    @Test
    @DisplayName("GET /members/expiring returns expiring-members view")
    void testExpiringMembersPage() throws Exception {
        mockMvc.perform(get("/members/expiring")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("expiring-members"))
                .andExpect(model().attributeExists("expiringMemberships"));
    }

    @Test
    @DisplayName("GET /attendance returns attendance tracker view")
    void testAttendancePage() throws Exception {
        mockMvc.perform(get("/attendance")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance"))
                .andExpect(model().attributeExists("todayCheckIns", "monthCheckIns", "recentCheckIns", "members"));
    }

    @Test
    @DisplayName("GET /members/{id}/edit returns edit-member form view with member data and plans")
    void testEditMemberPage() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(get("/members/" + member.getId() + "/edit")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-member"))
                .andExpect(model().attributeExists("updateRequest", "memberId", "member", "plans"));
    }

    @Test
    @DisplayName("GET /members/{id}/edit for non-existent member redirects to /members with error")
    void testEditNonExistentMemberPage() throws Exception {
        mockMvc.perform(get("/members/9999/edit")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("errorMessage", "Member not found."));
    }

    @Test
    @DisplayName("POST /members/{id}/edit submits valid update and redirects to /members with success")
    void testSubmitEditMember() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(post("/members/" + member.getId() + "/edit")
                .sessionAttr("loggedInUser", "Gym Admin")
                .param("name", "Sethu Kamal")
                .param("phone", "9876543210")
                .param("email", "newemail@gmail.com")
                .param("planId", String.valueOf(plan.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("successMessage", "Member updated successfully."));
    }

    @Test
    @DisplayName("POST /members/{id}/edit with duplicate phone re-renders form with error")
    void testSubmitEditMemberDuplicatePhone() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());
        Member memberB = memberService.registerMember("Kamal", "9876543211", "kamal@gmail.com", plan.getId());

        mockMvc.perform(post("/members/" + memberB.getId() + "/edit")
                .sessionAttr("loggedInUser", "Gym Admin")
                .param("name", "Kamal Updated")
                .param("phone", "9876543210")
                .param("email", "kamal@gmail.com")
                .param("planId", String.valueOf(plan.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-member"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /members/{id}/delete deletes member and redirects to /members with success")
    void testDeleteMemberAction() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(post("/members/" + member.getId() + "/delete")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("successMessage", "Member deleted successfully."));
    }

    @Test
    @DisplayName("POST /members/{id}/delete for non-existent member redirects to /members with error")
    void testDeleteNonExistentMemberAction() throws Exception {
        mockMvc.perform(post("/members/9999/delete")
                .sessionAttr("loggedInUser", "Gym Admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attribute("errorMessage", "Member not found."));
    }
}
