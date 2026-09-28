package com.example.GymFlex.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.example.GymFlex.models.Member;
import com.example.GymFlex.models.Membership;

public class MemberListItemDto {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String planName;
    private LocalDate expiryDate;
    private long daysRemaining;
    private String status; // "ACTIVE", "EXPIRING_SOON", "EXPIRED", "NO_MEMBERSHIP"

    public MemberListItemDto() {
    }

    public MemberListItemDto(Member member, Membership membership) {
        this.id = member.getId();
        this.name = member.getName();
        this.phone = member.getPhone();
        this.email = member.getEmail();

        if (membership != null) {
            this.planName = membership.getPlan() != null ? membership.getPlan().getName() : "N/A";
            this.expiryDate = membership.getExpiryDate();
            if (this.expiryDate != null) {
                LocalDate today = LocalDate.now();
                this.daysRemaining = ChronoUnit.DAYS.between(today, this.expiryDate);
                if (this.daysRemaining < 0) {
                    this.status = "EXPIRED";
                } else if (this.daysRemaining <= 7) {
                    this.status = "EXPIRING_SOON";
                } else {
                    this.status = "ACTIVE";
                }
            } else {
                this.status = "ACTIVE";
            }
        } else {
            this.planName = "No Plan";
            this.status = "NO_MEMBERSHIP";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
