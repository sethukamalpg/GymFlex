package com.example.GymFlex.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GymFlex.models.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhone(String phone);

    Optional<Member> findByPhone(String phone);
}