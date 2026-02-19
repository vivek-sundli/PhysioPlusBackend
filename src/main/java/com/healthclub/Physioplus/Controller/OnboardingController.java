package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Service.OnboardingService;
import com.healthclub.Physioplus.Dto.OnboardingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @Autowired
    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Initiates the onboarding process for a user.
     * Endpoint: POST /api/onboarding/start/{userId}
     */
    @PostMapping("/start/{userId}")
    public ResponseEntity<OnboardingResponse> startOnboarding(@PathVariable Long userId) {
        OnboardingResponse response = onboardingService.initiateOnboarding(userId);
        return ResponseEntity.ok(response);
    }
}