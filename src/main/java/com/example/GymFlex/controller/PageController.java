package com.example.GymFlex.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import com.example.GymFlex.dto.MemberListItemDto;
import com.example.GymFlex.dto.RegisterMemberRequest;
import com.example.GymFlex.exception.DuplicateResourceException;
import com.example.GymFlex.exception.MembershipExpiredException;
import com.example.GymFlex.exception.ResourceNotFoundException;
import com.example.GymFlex.models.CheckIn;
import com.example.GymFlex.models.Member;
import com.example.GymFlex.models.Membership;
import com.example.GymFlex.services.MemberService;
import com.example.GymFlex.services.PlanService;

import jakarta.validation.Valid;

@Controller
public class PageController {

    private final MemberService memberService;
    private final PlanService planService;

    @Value("${gymflex.admin.username:admin}")
    private String adminUsername;

    @Value("${gymflex.admin.password:admin123}")
    private String adminPassword;

    public PageController(MemberService memberService, PlanService planService) {
        this.memberService = memberService;
        this.planService = planService;
    }

    // 1. Login Page (Root URL)
    @GetMapping("/")
    public String home(
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            Model model) {

        if (session != null && session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }

        if ("unauthorized".equals(error)) {
            model.addAttribute("error", "Please login to access the dashboard.");
        }

        return "login";
    }

    // 1b. Process Login
    @PostMapping("/login")
    public String login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        if (username != null && adminUsername.equals(username.trim()) && adminPassword.equals(password)) {
            session.setAttribute("loggedInUser", "Gym Admin");
            session.setAttribute("username", username);
            session.setAttribute("userRole", "System Manager");
            return "redirect:/dashboard";
        }

        model.addAttribute("error", "Invalid username or password.");
        model.addAttribute("username", username);
        return "login";
    }

    // 1c. Process Logout
    @GetMapping("/logout")
    public String logoutGet(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logoutPost(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/";
    }

    // 2. Main Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activeNav", "dashboard");
        model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        model.addAttribute("totalMembers", memberService.getMemberCount());
        model.addAttribute("activeMembers", memberService.getActiveMembershipCount());
        model.addAttribute("expiringSoonCount", memberService.getExpiringMembershipCount());
        model.addAttribute("todayCheckIns", memberService.getTodayCheckInCount());

        model.addAttribute("recentCheckIns", memberService.getRecentCheckIns());
        model.addAttribute("expiringMemberships", memberService.getExpiringMembers());
        return "dashboard";
    }

    // 3. Members Page
    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("activeNav", "members");
        List<MemberListItemDto> members = memberService.getMembersListDto();
        model.addAttribute("members", members);
        return "members";
    }

    // 4. Add Member Page Form
    @GetMapping("/members/add")
    public String addMemberForm(Model model) {
        model.addAttribute("activeNav", "members");
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterMemberRequest());
        }
        model.addAttribute("plans", planService.getAllPlans());
        return "add-member";
    }

    // 5. Submit Add Member
    @PostMapping("/members/add")
    public String addMemberSubmit(
            @Valid @ModelAttribute("registerRequest") RegisterMemberRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("activeNav", "members");
            model.addAttribute("plans", planService.getAllPlans());
            return "add-member";
        }

        try {
            Member saved = memberService.registerMember(request);
            redirectAttributes.addFlashAttribute("successMessage", "Member '" + saved.getName() + "' registered successfully with chosen plan!");
            return "redirect:/members/" + saved.getId();
        } catch (DuplicateResourceException e) {
            model.addAttribute("activeNav", "members");
            model.addAttribute("plans", planService.getAllPlans());
            model.addAttribute("errorMessage", e.getMessage());
            return "add-member";
        } catch (Exception e) {
            model.addAttribute("activeNav", "members");
            model.addAttribute("plans", planService.getAllPlans());
            model.addAttribute("errorMessage", "Error registering member: " + e.getMessage());
            return "add-member";
        }
    }

    // 6. Member Details Page
    @GetMapping("/members/{id}")
    public String memberDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("activeNav", "members");
        try {
            Member member = memberService.getMemberById(id);
            Membership membership = memberService.getMembershipByMemberId(id).orElse(null);
            long currentMonthAttendance = memberService.getCurrentMonthAttendance(id);
            List<CheckIn> checkIns = memberService.getMemberCheckIns(id);

            model.addAttribute("member", member);
            model.addAttribute("membership", membership);
            model.addAttribute("attendanceCount", currentMonthAttendance);
            model.addAttribute("checkIns", checkIns);

            String status = "NO_MEMBERSHIP";
            if (membership != null && membership.getExpiryDate() != null) {
                LocalDate today = LocalDate.now();
                if (membership.getExpiryDate().isBefore(today)) {
                    status = "EXPIRED";
                } else if (!membership.getExpiryDate().isAfter(today.plusDays(7))) {
                    status = "EXPIRING_SOON";
                } else {
                    status = "ACTIVE";
                }
            }
            model.addAttribute("membershipStatus", status);

            return "member-details";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Member not found with id: " + id);
            return "redirect:/members";
        }
    }

    // 7. Check In Action from UI
    @PostMapping("/members/{id}/checkin")
    public String checkInMember(
            @PathVariable Long id,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {

        try {
            CheckIn checkIn = memberService.checkIn(id);
            redirectAttributes.addFlashAttribute("successMessage", "✓ Check-in recorded successfully for " + checkIn.getMember().getName() + "!");
        } catch (MembershipExpiredException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "✕ Check-in rejected — this membership has expired.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "✕ Check-in failed: " + e.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/members/" + id;
    }

    // 8. Renew Membership Action from UI
    @PostMapping("/members/{id}/renew")
    public String renewMember(
            @PathVariable Long id,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            RedirectAttributes redirectAttributes) {

        try {
            Membership renewed = memberService.renewMembership(id);
            redirectAttributes.addFlashAttribute("successMessage", "✓ Membership renewed successfully! New expiry date: " + renewed.getExpiryDate());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "✕ Renewal failed: " + e.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/members/" + id;
    }

    // 9. Plans Page
    @GetMapping("/plans")
    public String plans(Model model) {
        model.addAttribute("activeNav", "plans");
        model.addAttribute("plans", planService.getAllPlans());
        return "plans";
    }

    // 10. Expiring Members Page
    @GetMapping("/members/expiring")
    public String expiringMembers(Model model) {
        model.addAttribute("activeNav", "expiring");
        List<Membership> expiringList = memberService.getExpiringMembers();
        model.addAttribute("expiringMemberships", expiringList);
        return "expiring-members";
    }

    // 11. Attendance Tracking Page
    @GetMapping("/attendance")
    public String attendance(Model model) {
        model.addAttribute("activeNav", "attendance");
        model.addAttribute("todayCheckIns", memberService.getTodayCheckInCount());
        model.addAttribute("monthCheckIns", memberService.getCurrentMonthCheckInCount());
        model.addAttribute("recentCheckIns", memberService.getRecentCheckIns());
        model.addAttribute("members", memberService.getMembersListDto());
        return "attendance";
    }
}
