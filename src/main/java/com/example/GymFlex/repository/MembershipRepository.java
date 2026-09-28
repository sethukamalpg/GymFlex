package com.example.GymFlex.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GymFlex.models.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByMemberId(Long memberId);

    List<Membership> findByExpiryDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    long countByExpiryDateGreaterThanEqual(LocalDate date);

    long countByExpiryDateBetween(LocalDate start, LocalDate end);
}