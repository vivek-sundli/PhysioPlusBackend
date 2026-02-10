// DTO Helper Class
package com.healthclub.Physioplus.Dto;

public class OnboardingResponse {
    private boolean success;
    private String message;
    private int currentStep;

    public OnboardingResponse(boolean success, String message, int currentStep) {
        this.success = success;
        this.message = message;
        this.currentStep = currentStep;
    }

    // Getters and Setters...
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getCurrentStep() { return currentStep; }
}