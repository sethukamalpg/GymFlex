package com.example.GymFlex.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.GymFlex.models.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    boolean existsByName(String name);
}