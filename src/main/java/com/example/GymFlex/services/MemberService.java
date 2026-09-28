package com.example.GymFlex.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.GymFlex.dto.MemberListItemDto;
import com.example.GymFlex.dto.RegisterMemberRequest;
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

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final MembershipRepository membershipRepository;
    private final CheckInRepository checkInRepository;

    public MemberService(
            MemberRepository memberRepository,
            PlanRepository planRepository,
            MembershipRepository membershipRepository,
            CheckInRepository checkInRepository) {

        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.membershipRepository = membershipRepository;
        this.checkInRepository = checkInRepository;
    }

    // 1. Register member with DTO
    @Transactional
    public Member registerMember(RegisterMemberRequest request) {
        return registerMember(
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getPlanId()
        );
    }

    // 1. Overloaded register member for flexibility
    @Transactional
    public Member registerMember(
            String name,
            String phone,
            String email,
            Long planId) {

        if (memberRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Member with phone '" + phone + "' already exists");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + planId));

        Member member = new Member(name, phone, email);
        Member savedMember = memberRepository.save(member);

        LocalDate startDate = LocalDate.now();
        LocalDate expiryDate = startDate.plusMonths(plan.getDurationMonths());

        Membership membership = new Membership();
        membership.setMember(savedMember);
        membership.setPlan(plan);
        membership.setStartDate(startDate);
        membership.setExpiryDate(expiryDate);

        membershipRepository.save(membership);

        return savedMember;
    }

    // 2. Renew membership
    @Transactional
    public Membership renewMembership(Long memberId) {

        // Verify member exists
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        Membership membership = membershipRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for member ID: " + memberId));

        Plan plan = membership.getPlan();

        // BUSINESS RULE 2:
        // Renewal starts from EXISTING EXPIRY DATE, NOT from today's date.
        // newExpiryDate = currentExpiryDate + plan.durationMonths
        LocalDate newExpiryDate =
                membership.getExpiryDate()
                        .plusMonths(plan.getDurationMonths());

        membership.setExpiryDate(newExpiryDate);

        return membershipRepository.save(membership);
    }

    // 3. Check-in
    @Transactional
    public CheckIn checkIn(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        Membership membership = membershipRepository
                .findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for member ID: " + memberId));

        LocalDate today = LocalDate.now();

        // BUSINESS RULE 1:
        // Check-in rejected if membership expired (expiryDate < today).
        // Validation MUST happen BEFORE creating or saving the CheckIn.
        if (membership.getExpiryDate().isBefore(today)) {
            throw new MembershipExpiredException(
                    "Check-in rejected. Membership has expired."
            );
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setMember(member);
        checkIn.setCheckInTime(LocalDateTime.now());

        return checkInRepository.save(checkIn);
    }

    // 4. Members whose membership expires within next 7 days
    public List<Membership> getExpiringMembers() {

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        return membershipRepository.findByExpiryDateBetween(
                today,
                sevenDaysLater
        );
    }

    // 5. Attendance count for current month
    public long getCurrentMonthAttendance(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        LocalDateTime startOfMonth =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime startOfNextMonth =
                LocalDate.now()
                        .plusMonths(1)
                        .withDayOfMonth(1)
                        .atStartOfDay();

        return checkInRepository.countByMemberIdAndCheckInTimeBetween(
                memberId,
                startOfMonth,
                startOfNextMonth
        );
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public List<MemberListItemDto> getMembersListDto() {
        List<Member> members = memberRepository.findAll();
        List<MemberListItemDto> dtos = new ArrayList<>();
        for (Member m : members) {
            Membership ms = membershipRepository.findByMemberId(m.getId()).orElse(null);
            dtos.add(new MemberListItemDto(m, ms));
        }
        return dtos;
    }

    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
    }

    public java.util.Optional<Membership> getMembershipByMemberId(Long memberId) {
        return membershipRepository.findByMemberId(memberId);
    }

    public List<CheckIn> getRecentCheckIns() {
        return checkInRepository.findTop10ByOrderByCheckInTimeDesc();
    }

    public List<CheckIn> getMemberCheckIns(Long memberId) {
        return checkInRepository.findByMemberIdOrderByCheckInTimeDesc(memberId);
    }

    public long getMemberCount() {
        return memberRepository.count();
    }

    public long getActiveMembershipCount() {
        return membershipRepository.countByExpiryDateGreaterThanEqual(LocalDate.now());
    }

    public long getExpiringMembershipCount() {
        return membershipRepository.countByExpiryDateBetween(
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );
    }

    public long getTodayCheckInCount() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        return checkInRepository.countByCheckInTimeBetween(startOfToday, startOfTomorrow);
    }

    public long getCurrentMonthCheckInCount() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();
        return checkInRepository.countByCheckInTimeBetween(startOfMonth, startOfNextMonth);
    }
}