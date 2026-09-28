package com.example.GymFlex.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.GymFlex.exception.BadRequestException;
import com.example.GymFlex.exception.DuplicateResourceException;
import com.example.GymFlex.exception.ResourceNotFoundException;
import com.example.GymFlex.models.Plan;
import com.example.GymFlex.repository.PlanRepository;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Plan createPlan(Plan plan) {
        if (plan.getName() == null || plan.getName().isBlank()) {
            throw new BadRequestException("Plan name is required");
        }

        if (plan.getDurationMonths() <= 0) {
            throw new BadRequestException("Duration must be greater than zero");
        }

        if (plan.getPrice() <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }

        if (planRepository.existsByName(plan.getName())) {
            throw new DuplicateResourceException("Plan with name '" + plan.getName() + "' already exists");
        }

        return planRepository.save(plan);
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }
}