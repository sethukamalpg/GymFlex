package com.example.GymFlex.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GymFlex.models.CheckIn;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    long countByMemberIdAndCheckInTimeBetween(
            Long memberId,
            LocalDateTime start,
            LocalDateTime end
    );
}