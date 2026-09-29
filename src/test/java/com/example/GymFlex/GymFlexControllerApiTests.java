package com.example.GymFlex;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.GymFlex.controller.MemberController;
import com.example.GymFlex.controller.PlanController;
import com.example.GymFlex.exception.GlobalExceptionHandler;
import com.example.GymFlex.models.Member;
import com.example.GymFlex.models.Membership;
import com.example.GymFlex.models.Plan;
import com.example.GymFlex.repository.CheckInRepository;
import com.example.GymFlex.repository.MemberRepository;
import com.example.GymFlex.repository.MembershipRepository;
import com.example.GymFlex.repository.PlanRepository;
import com.example.GymFlex.services.MemberService;
import com.example.GymFlex.services.PlanService;

@SpringBootTest
public class GymFlexControllerApiTests {

    private MockMvc mockMvc;

    @Autowired
    private MemberController memberController;

    @Autowired
    private PlanController planController;

    @Autowired
    private PlanService planService;

    @Autowired
    private MemberService memberService;

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
        mockMvc = MockMvcBuilders.standaloneSetup(memberController, planController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        checkInRepository.deleteAll();
        membershipRepository.deleteAll();
        memberRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/plans creates plan (201)")
    void testCreatePlanApi() throws Exception {
        String json = """
            {
                "name": "Monthly",
                "durationMonths": 1,
                "price": 1000.0
            }
        """;

        mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Monthly"))
                .andExpect(jsonPath("$.durationMonths").value(1))
                .andExpect(jsonPath("$.price").value(1000.0));
    }

    @Test
    @DisplayName("POST /api/plans with invalid input returns 400 Bad Request")
    void testCreatePlanValidationApi() throws Exception {
        String invalidJson = """
            {
                "name": "",
                "durationMonths": 0,
                "price": -50.0
            }
        """;

        mockMvc.perform(post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/plans returns all plans (200)")
    void testGetAllPlansApi() throws Exception {
        planService.createPlan(new Plan("Monthly", 1, 1000.0));
        planService.createPlan(new Plan("Yearly", 12, 9000.0));

        mockMvc.perform(get("/api/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/members registers member with plan (201)")
    void testRegisterMemberApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));

        String memberJson = String.format("""
            {
                "name": "Sethu",
                "phone": "9876543210",
                "email": "sethu@gmail.com",
                "planId": %d
            }
        """, plan.getId());

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(memberJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Sethu"))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.email").value("sethu@gmail.com"));
    }

    @Test
    @DisplayName("POST /api/members with duplicate phone returns 409 Conflict")
    void testDuplicatePhoneApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        String duplicateJson = String.format("""
            {
                "name": "Another",
                "phone": "9876543210",
                "email": "another@gmail.com",
                "planId": %d
            }
        """, plan.getId());

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Member with phone '9876543210' already exists"));
    }

    @Test
    @DisplayName("POST /api/members/{id}/checkin creates check-in for active member (201)")
    void testCheckInApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(post("/api/members/" + member.getId() + "/checkin"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.member.id").value(member.getId()))
                .andExpect(jsonPath("$.checkInTime").exists());
    }

    @Test
    @DisplayName("POST /api/members/{id}/checkin rejects expired membership (400)")
    void testCheckInExpiredApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        Membership membership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        membership.setExpiryDate(LocalDate.now().minusDays(1));
        membershipRepository.save(membership);

        mockMvc.perform(post("/api/members/" + member.getId() + "/checkin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Check-in rejected. Membership has expired."));
    }

    @Test
    @DisplayName("GET /api/members/{id}/attendance returns monthly attendance count (200)")
    void testAttendanceApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        memberService.checkIn(member.getId());

        mockMvc.perform(get("/api/members/" + member.getId() + "/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(member.getId()))
                .andExpect(jsonPath("$.currentMonthAttendance").value(1));
    }

    @Test
    @DisplayName("PUT /api/members/{id}/renew extends expiry from current expiry date (200)")
    void testRenewMembershipApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        LocalDate fixedExpiryDate = LocalDate.of(2026, 9, 30);
        Membership membership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        membership.setExpiryDate(fixedExpiryDate);
        membershipRepository.save(membership);

        mockMvc.perform(put("/api/members/" + member.getId() + "/renew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiryDate").value("2026-10-30"));
    }

    @Test
    @DisplayName("GET /api/members/expiring returns members expiring within 7 days (200)")
    void testExpiringMembersApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        Membership membership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        membership.setExpiryDate(LocalDate.now().plusDays(3));
        membershipRepository.save(membership);

        mockMvc.perform(get("/api/members/expiring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].member.id").value(member.getId()));
    }

    @Test
    @DisplayName("PUT /api/members/{id} updates member details (200)")
    void testUpdateMemberApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        String updateJson = String.format("""
            {
                "name": "Sethu Kamal",
                "phone": "9876543210",
                "email": "updated@gmail.com",
                "planId": %d
            }
        """, plan.getId());

        mockMvc.perform(put("/api/members/" + member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()))
                .andExpect(jsonPath("$.name").value("Sethu Kamal"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));
    }

    @Test
    @DisplayName("PUT /api/members/{id} with invalid data returns 400 Bad Request")
    void testUpdateMemberValidationApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        String invalidJson = """
            {
                "name": "",
                "phone": "",
                "email": "not-an-email",
                "planId": 0
            }
        """;

        mockMvc.perform(put("/api/members/" + member.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/members/{id} with duplicate phone of another member returns 409 Conflict")
    void testUpdateMemberDuplicatePhoneApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());
        Member memberB = memberService.registerMember("Kamal", "9876543211", "kamal@gmail.com", plan.getId());

        String updateJson = String.format("""
            {
                "name": "Kamal",
                "phone": "9876543210",
                "email": "kamal@gmail.com",
                "planId": %d
            }
        """, plan.getId());

        mockMvc.perform(put("/api/members/" + memberB.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Phone number already exists."));
    }

    @Test
    @DisplayName("PUT /api/members/{id} for non-existent member returns 404 Not Found")
    void testUpdateNonExistentMemberApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));

        String updateJson = String.format("""
            {
                "name": "Ghost",
                "phone": "9999999999",
                "email": "ghost@gmail.com",
                "planId": %d
            }
        """, plan.getId());

        mockMvc.perform(put("/api/members/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Member not found."));
    }

    @Test
    @DisplayName("DELETE /api/members/{id} successfully deletes member (200)")
    void testDeleteMemberApi() throws Exception {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        mockMvc.perform(delete("/api/members/" + member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member deleted successfully."));

        // Verify member is gone
        mockMvc.perform(get("/api/members/" + member.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Member not found."));
    }

    @Test
    @DisplayName("DELETE /api/members/{id} for non-existent member returns 404 Not Found")
    void testDeleteNonExistentMemberApi() throws Exception {
        mockMvc.perform(delete("/api/members/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Member not found."));
    }
}
