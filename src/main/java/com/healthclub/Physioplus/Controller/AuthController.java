package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Dto.AuthResponse;
import com.healthclub.Physioplus.Dto.SendOtpRequest;
import com.healthclub.Physioplus.Dto.VerifyOtpRequest;
import com.healthclub.Physioplus.Model.User;
import com.healthclub.Physioplus.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/send-otp
     * Send OTP to email or phone
     *
     * Request body:
     * {
     *   "email": "user@example.com",  // Required if type is EMAIL
     *   "phone": "+919876543210",     // Required if type is SMS
     *   "type": "EMAIL",              // EMAIL or SMS
     *   "name": "John Doe",           // Optional, for new users
     *   "role": "PATIENT"             // PATIENT or DOCTOR, defaults to PATIENT
     * }
     */
    @PostMapping("/send-otp")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        AuthResponse response = authService.sendOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * POST /api/auth/verify-otp
     * Verify OTP and return JWT token
     *
     * Request body:
     * {
     *   "email": "user@example.com",  // Required if type is EMAIL
     *   "phone": "+919876543210",     // Required if type is SMS
     *   "otp": "123456",
     *   "type": "EMAIL"               // EMAIL or SMS
     * }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * GET /api/auth/me
     * Get current user profile from JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(401).body(AuthResponse.error("Authorization header required"));
        }

        Optional<User> user = authService.getUserFromToken(authHeader);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(401).body(AuthResponse.error("Invalid or expired token"));
    }

    /**
     * PUT /api/auth/profile
     * Update user profile
     *
     * Request body:
     * {
     *   "name": "New Name",
     *   "email": "new@example.com",
     *   "phone": "+919876543210"
     * }
     */
    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {

        Optional<User> userOpt = authService.getUserFromToken(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(AuthResponse.error("Invalid or expired token"));
        }

        AuthResponse response = authService.updateUserProfile(
                userOpt.get().getId(),
                request.getName(),
                request.getEmail(),
                request.getPhone()
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Inner class for profile update request
    public static class UpdateProfileRequest {
        private String name;
        private String email;
        private String phone;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
}
