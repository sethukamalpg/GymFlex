package com.example.GymFlex.dto;

public class AttendanceResponse {

    private Long memberId;
    private long currentMonthAttendance;

    public AttendanceResponse() {
    }

    public AttendanceResponse(Long memberId, long currentMonthAttendance) {
        this.memberId = memberId;
        this.currentMonthAttendance = currentMonthAttendance;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public long getCurrentMonthAttendance() {
        return currentMonthAttendance;
    }

    public void setCurrentMonthAttendance(long currentMonthAttendance) {
        this.currentMonthAttendance = currentMonthAttendance;
    }
}
