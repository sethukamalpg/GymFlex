package com.example.GymFlex;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.GymFlex.dto.RegisterMemberRequest;
import com.example.GymFlex.dto.UpdateMemberRequest;
import com.example.GymFlex.exception.BadRequestException;
import com.example.GymFlex.exception.DuplicateResourceException;
import com.example.GymFlex.exception.MembershipExpiredException;
import com.example.GymFlex.exception.ResourceNotFoundException;
import com.example.GymFlex.models.CheckIn;
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
public class GymFlexBusinessLogicTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PlanService planService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @BeforeEach
    void setUp() {
        checkInRepository.deleteAll();
        membershipRepository.deleteAll();
        memberRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    @DisplayName("Create plans and retrieve them")
    void testCreatePlans() {
        Plan monthly = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Plan quarterly = planService.createPlan(new Plan("Quarterly", 3, 2500.0));
        Plan yearly = planService.createPlan(new Plan("Yearly", 12, 9000.0));

        assertNotNull(monthly.getId());
        assertNotNull(quarterly.getId());
        assertNotNull(yearly.getId());

        List<Plan> plans = planService.getAllPlans();
        assertEquals(3, plans.size());
    }

    @Test
    @DisplayName("Plan validation rejects invalid data")
    void testPlanValidation() {
        assertThrows(BadRequestException.class, () -> {
            planService.createPlan(new Plan("", 1, 1000.0));
        });

        assertThrows(BadRequestException.class, () -> {
            planService.createPlan(new Plan("Free", 0, 1000.0));
        });

        assertThrows(BadRequestException.class, () -> {
            planService.createPlan(new Plan("Negative", 1, -50.0));
        });
    }

    @Test
    @DisplayName("Register member with plan creates member and membership")
    void testRegisterMember() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));

        RegisterMemberRequest request = new RegisterMemberRequest(
                "Sethu", "9876543210", "sethu@gmail.com", plan.getId()
        );

        Member member = memberService.registerMember(request);
        assertNotNull(member.getId());
        assertEquals("Sethu", member.getName());

        Membership membership = membershipRepository.findByMemberId(member.getId()).orElse(null);
        assertNotNull(membership);
        assertEquals(LocalDate.now(), membership.getStartDate());
        assertEquals(LocalDate.now().plusMonths(1), membership.getExpiryDate());
    }

    @Test
    @DisplayName("Duplicate phone registration throws DuplicateResourceException")
    void testDuplicatePhone() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));

        RegisterMemberRequest req1 = new RegisterMemberRequest(
                "Sethu", "9876543210", "sethu@gmail.com", plan.getId()
        );
        memberService.registerMember(req1);

        RegisterMemberRequest req2 = new RegisterMemberRequest(
                "Kamal", "9876543210", "kamal@gmail.com", plan.getId()
        );

        assertThrows(DuplicateResourceException.class, () -> {
            memberService.registerMember(req2);
        });
    }

    @Test
    @DisplayName("Register with non-existent plan throws ResourceNotFoundException")
    void testInvalidPlanRegistration() {
        RegisterMemberRequest request = new RegisterMemberRequest(
                "Sethu", "9876543210", "sethu@gmail.com", 9999L
        );

        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.registerMember(request);
        });
    }

    @Test
    @DisplayName("RULE 1: Check-in succeeds for active membership, increments count")
    void testActiveCheckIn() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        CheckIn checkIn = memberService.checkIn(member.getId());
        assertNotNull(checkIn.getId());
        assertEquals(member.getId(), checkIn.getMember().getId());

        long attendance = memberService.getCurrentMonthAttendance(member.getId());
        assertEquals(1, attendance);
    }

    @Test
    @DisplayName("RULE 1: Check-in rejected when membership expired, NO record created")
    void testExpiredCheckInRejected() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        // Force expiry date to yesterday
        Membership membership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        membership.setExpiryDate(LocalDate.now().minusDays(1));
        membershipRepository.save(membership);

        MembershipExpiredException ex = assertThrows(MembershipExpiredException.class, () -> {
            memberService.checkIn(member.getId());
        });

        assertEquals("Check-in rejected. Membership has expired.", ex.getMessage());

        // Verify NO CheckIn record was created
        long count = checkInRepository.countByMemberIdAndCheckInTimeBetween(
                member.getId(),
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        );
        assertEquals(0, count);
    }

    @Test
    @DisplayName("RULE 2: Renewal extends from CURRENT EXPIRY DATE, not from today")
    void testRenewalExtendsFromExistingExpiryDate() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        // Set a known fixed expiry date: 2026-09-30
        LocalDate fixedExpiryDate = LocalDate.of(2026, 9, 30);
        Membership membership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        membership.setExpiryDate(fixedExpiryDate);
        membershipRepository.save(membership);

        // Renew membership (1-month plan duration)
        Membership renewed = memberService.renewMembership(member.getId());

        // Expected new expiry date = 2026-09-30 + 1 month = 2026-10-30
        LocalDate expectedExpiryDate = LocalDate.of(2026, 10, 30);
        assertEquals(expectedExpiryDate, renewed.getExpiryDate());
    }

    @Test
    @DisplayName("Get members expiring within 7 days")
    void testExpiringMembers() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        // Set expiry date to 4 days from today
        Membership membership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        membership.setExpiryDate(LocalDate.now().plusDays(4));
        membershipRepository.save(membership);

        List<Membership> expiring = memberService.getExpiringMembers();
        assertEquals(1, expiring.size());
        assertEquals(member.getId(), expiring.get(0).getMember().getId());
    }

    @Test
    @DisplayName("Attendance for member with zero check-ins returns 0")
    void testZeroAttendance() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        long attendance = memberService.getCurrentMonthAttendance(member.getId());
        assertEquals(0, attendance);
    }

    @Test
    @DisplayName("Update member details updates name, email and preserves membership when plan unchanged")
    void testUpdateMemberDetails() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        Membership originalMembership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        LocalDate originalExpiry = originalMembership.getExpiryDate();

        UpdateMemberRequest updateReq = new UpdateMemberRequest(
                "Sethu Kamal", "9876543210", "kamal@gmail.com", plan.getId()
        );

        Member updated = memberService.updateMember(member.getId(), updateReq);
        assertEquals("Sethu Kamal", updated.getName());
        assertEquals("kamal@gmail.com", updated.getEmail());
        assertEquals("9876543210", updated.getPhone());

        Membership currentMembership = membershipRepository.findByMemberId(member.getId()).orElseThrow();
        assertEquals(originalExpiry, currentMembership.getExpiryDate());
    }

    @Test
    @DisplayName("Update membership plan updates existing membership record with new duration from today")
    void testUpdateMemberPlan() {
        Plan monthly = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Plan yearly = planService.createPlan(new Plan("Yearly", 12, 9000.0));

        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", monthly.getId());

        UpdateMemberRequest updateReq = new UpdateMemberRequest(
                "Sethu", "9876543210", "sethu@gmail.com", yearly.getId()
        );

        memberService.updateMember(member.getId(), updateReq);

        // Verify only 1 membership record exists (no duplicate created)
        List<Membership> allMemberships = membershipRepository.findAll();
        assertEquals(1, allMemberships.size());

        Membership updatedMembership = allMemberships.get(0);
        assertEquals(yearly.getId(), updatedMembership.getPlan().getId());
        assertEquals(LocalDate.now(), updatedMembership.getStartDate());
        assertEquals(LocalDate.now().plusMonths(12), updatedMembership.getExpiryDate());
    }

    @Test
    @DisplayName("Update phone to another member's phone throws DuplicateResourceException")
    void testUpdateDuplicatePhoneRejected() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member memberA = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());
        Member memberB = memberService.registerMember("Kamal", "9876543211", "kamal@gmail.com", plan.getId());

        UpdateMemberRequest updateReq = new UpdateMemberRequest(
                "Kamal Updated", "9876543210", "kamal@gmail.com", plan.getId()
        );

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> {
            memberService.updateMember(memberB.getId(), updateReq);
        });

        assertEquals("Phone number already exists.", ex.getMessage());
    }

    @Test
    @DisplayName("Update keeping own phone number is allowed")
    void testUpdateSamePhoneAllowed() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        UpdateMemberRequest updateReq = new UpdateMemberRequest(
                "Sethu Updated", "9876543210", "newsethu@gmail.com", plan.getId()
        );

        Member updated = memberService.updateMember(member.getId(), updateReq);
        assertEquals("Sethu Updated", updated.getName());
        assertEquals("9876543210", updated.getPhone());
    }

    @Test
    @DisplayName("Delete member safely removes member, check-ins, and membership without FK errors")
    void testDeleteMemberSafely() {
        Plan plan = planService.createPlan(new Plan("Monthly", 1, 1000.0));
        Member member = memberService.registerMember("Sethu", "9876543210", "sethu@gmail.com", plan.getId());

        // Create check-in
        memberService.checkIn(member.getId());
        assertEquals(1, checkInRepository.findByMemberIdOrderByCheckInTimeDesc(member.getId()).size());
        assertTrue(membershipRepository.findByMemberId(member.getId()).isPresent());

        // Delete member
        memberService.deleteMember(member.getId());

        // Verify member, check-ins, and membership are permanently removed
        assertTrue(memberRepository.findById(member.getId()).isEmpty());
        assertTrue(membershipRepository.findByMemberId(member.getId()).isEmpty());
        assertEquals(0, checkInRepository.findByMemberIdOrderByCheckInTimeDesc(member.getId()).size());
    }

    @Test
    @DisplayName("Delete non-existent member throws ResourceNotFoundException")
    void testDeleteNonExistentMember() {
        assertThrows(ResourceNotFoundException.class, () -> {
            memberService.deleteMember(9999L);
        });
    }
}
