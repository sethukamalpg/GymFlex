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

import com.example.GymFlex.controller.PageController;
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
    private org.springframework.web.context.WebApplicationContext webApplicationContext;

    @Autowired
    private PageController pageController;

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
    @DisplayName("GET / returns index view")
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("totalMembers", "activeMembers", "todayCheckIns"));
    }

    @Test
    @DisplayName("GET /dashboard returns dashboard view with metrics")
    void testDashboardPage() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("totalMembers", "activeMembers", "expiringSoonCount", "todayCheckIns"));
    }

    @Test
    @DisplayName("GET /members returns members view")
    void testMembersPage() throws Exception {
        mockMvc.perform(get("/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("members"))
                .andExpect(model().attributeExists("members"));
    }

    @Test
    @DisplayName("GET /members/add returns add-member form view")
    void testAddMemberPage() throws Exception {
        mockMvc.perform(get("/members/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add-member"))
                .andExpect(model().attributeExists("registerRequest", "plans"));
    }

    @Test
    @DisplayName("POST /members/add submits form and redirects to member profile")
    void testSubmitAddMember() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));

        mockMvc.perform(post("/members/add")
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

        mockMvc.perform(get("/members/" + member.getId()))
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
                .param("redirectUrl", "/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /members/{id}/renew renews plan and redirects")
    void testRenewAction() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(post("/members/" + member.getId() + "/renew")
                .param("redirectUrl", "/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/members"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("GET /plans returns plans view with dynamic plan cards")
    void testPlansPage() throws Exception {
        mockMvc.perform(get("/plans"))
                .andExpect(status().isOk())
                .andExpect(view().name("plans"))
                .andExpect(model().attributeExists("plans"));
    }

    @Test
    @DisplayName("GET /members/expiring returns expiring-members view")
    void testExpiringMembersPage() throws Exception {
        mockMvc.perform(get("/members/expiring"))
                .andExpect(status().isOk())
                .andExpect(view().name("expiring-members"))
                .andExpect(model().attributeExists("expiringMemberships"));
    }

    @Test
    @DisplayName("GET /attendance returns attendance tracker view")
    void testAttendancePage() throws Exception {
        mockMvc.perform(get("/attendance"))
                .andExpect(status().isOk())
                .andExpect(view().name("attendance"))
                .andExpect(model().attributeExists("todayCheckIns", "monthCheckIns", "recentCheckIns", "members"));
    }
}
