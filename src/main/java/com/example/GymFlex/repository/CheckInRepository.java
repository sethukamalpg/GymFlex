package com.example.GymFlex.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GymFlex.models.CheckIn;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    long countByMemberIdAndCheckInTimeBetween(
            Long memberId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CheckIn> findTop10ByOrderByCheckInTimeDesc();

    List<CheckIn> findByMemberIdOrderByCheckInTimeDesc(Long memberId);

    void deleteByMemberId(Long memberId);
}