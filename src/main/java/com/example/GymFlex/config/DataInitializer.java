package com.example.GymFlex.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.GymFlex.models.Plan;
import com.example.GymFlex.repository.PlanRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PlanRepository planRepository;

    public DataInitializer(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            planRepository.save(new Plan("Monthly", 1, 1000.0));
            planRepository.save(new Plan("Quarterly", 3, 2500.0));
            planRepository.save(new Plan("Yearly", 12, 9000.0));
        }
    }
}
