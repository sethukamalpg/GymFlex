package com.example.GymFlex.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.GymFlex.dto.AttendanceResponse;
import com.example.GymFlex.dto.RegisterMemberRequest;
import com.example.GymFlex.dto.UpdateMemberRequest;
import com.example.GymFlex.models.CheckIn;
import com.example.GymFlex.models.Member;
import com.example.GymFlex.models.Membership;
import com.example.GymFlex.services.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 1. Register member
    @PostMapping
    public ResponseEntity<Member> registerMember(@Valid @RequestBody RegisterMemberRequest request) {
        Member savedMember = memberService.registerMember(request);
        return new ResponseEntity<>(savedMember, HttpStatus.CREATED);
    }

    // 2. Get all members
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    // 3. Get member by ID
    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.getMemberById(memberId));
    }

    // 4. Renew membership
    @PutMapping("/{memberId}/renew")
    public ResponseEntity<Membership> renewMembership(@PathVariable Long memberId) {
        Membership renewed = memberService.renewMembership(memberId);
        return ResponseEntity.ok(renewed);
    }

    // 5. Check-in
    @PostMapping("/{memberId}/checkin")
    public ResponseEntity<CheckIn> checkIn(@PathVariable Long memberId) {
        CheckIn checkIn = memberService.checkIn(memberId);
        return new ResponseEntity<>(checkIn, HttpStatus.CREATED);
    }

    // 6. Expiring within next 7 days
    @GetMapping("/expiring")
    public ResponseEntity<List<Membership>> getExpiringMembers() {
        return ResponseEntity.ok(memberService.getExpiringMembers());
    }

    // 7. Current month attendance count
    @GetMapping("/{memberId}/attendance")
    public ResponseEntity<AttendanceResponse> getAttendance(@PathVariable Long memberId) {
        long count = memberService.getCurrentMonthAttendance(memberId);
        return ResponseEntity.ok(new AttendanceResponse(memberId, count));
    }

    // 8. Update member
    @PutMapping("/{memberId}")
    public ResponseEntity<Member> updateMember(
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRequest request) {
        Member updatedMember = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(updatedMember);
    }

    // 9. Delete member
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Map<String, String>> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(Map.of("message", "Member deleted successfully."));
    }
}