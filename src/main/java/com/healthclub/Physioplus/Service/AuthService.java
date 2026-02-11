package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.*;
import com.healthclub.Physioplus.Model.OtpToken;
import com.healthclub.Physioplus.Model.User;
import com.healthclub.Physioplus.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository, OtpService otpService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    public AuthResponse sendOtp(SendOtpRequest request) {
        OtpType otpType;
        try {
            otpType = OtpType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AuthResponse.error("Invalid OTP type. Use EMAIL or SMS");
        }

        String target;
        if (otpType == OtpType.EMAIL) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return AuthResponse.error("Email is required for EMAIL OTP");
            }
            target = request.getEmail();
        } else {
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                return AuthResponse.error("Phone is required for SMS OTP");
            }
            target = request.getPhone();
        }

        // Find or create user
        User user = findOrCreateUser(request, otpType, target);

        try {
            otpService.createAndSendOtp(user.getId(), target, otpType);
            return new AuthResponse(true, "OTP sent successfully to " + target);
        } catch (Exception e) {
            return AuthResponse.error("Failed to send OTP: " + e.getMessage());
        }
    }

    private User findOrCreateUser(SendOtpRequest request, OtpType otpType, String target) {
        Optional<User> existingUser;
        if (otpType == OtpType.EMAIL) {
            existingUser = userRepository.findByEmail(target);
        } else {
            existingUser = userRepository.findByPhone(target);
        }

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user
        User newUser = new User();
        if (otpType == OtpType.EMAIL) {
            newUser.setEmail(target);
        } else {
            newUser.setPhone(target);
        }
        newUser.setName(request.getName() != null ? request.getName() : "User");
        newUser.setRole(request.getRole() != null ? request.getRole() : UserRole.PATIENT);
        newUser.setCreatedAt(Instant.now());
        newUser.setUpdatedAt(Instant.now());

        return userRepository.save(newUser);
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        OtpType otpType;
        try {
            otpType = OtpType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AuthResponse.error("Invalid OTP type. Use EMAIL or SMS");
        }

        String target;
        if (otpType == OtpType.EMAIL) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return AuthResponse.error("Email is required for EMAIL OTP verification");
            }
            target = request.getEmail();
        } else {
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                return AuthResponse.error("Phone is required for SMS OTP verification");
            }
            target = request.getPhone();
        }

        Optional<OtpToken> otpToken = otpService.verifyOtp(target, request.getOtp());
        if (otpToken.isEmpty()) {
            return AuthResponse.error("Invalid or expired OTP");
        }

        // Get user and mark as verified
        Optional<User> userOpt;
        if (otpType == OtpType.EMAIL) {
            userOpt = userRepository.findByEmail(target);
        } else {
            userOpt = userRepository.findByPhone(target);
        }

        if (userOpt.isEmpty()) {
            return AuthResponse.error("User not found");
        }

        User user = userOpt.get();
        if (otpType == OtpType.EMAIL) {
            user.setEmailVerified(true);
        } else {
            user.setPhoneVerified(true);
        }
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(user);

        // Clear sensitive data before returning
        return AuthResponse.success("OTP verified successfully", token, user);
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserFromToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtService.validateToken(token)) {
            return Optional.empty();
        }

        String userId = jwtService.getUserIdFromToken(token);
        return userRepository.findById(userId);
    }

    public AuthResponse updateUserProfile(String userId, String name, String email, String phone) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return AuthResponse.error("User not found");
        }

        User user = userOpt.get();
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
            // Check if email is already taken
            if (userRepository.existsByEmail(email)) {
                return AuthResponse.error("Email already in use");
            }
            user.setEmail(email);
            user.setEmailVerified(false);  // Require re-verification
        }
        if (phone != null && !phone.isBlank() && !phone.equals(user.getPhone())) {
            // Check if phone is already taken
            if (userRepository.existsByPhone(phone)) {
                return AuthResponse.error("Phone number already in use");
            }
            user.setPhone(phone);
            user.setPhoneVerified(false);  // Require re-verification
        }

        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return new AuthResponse(true, "Profile updated successfully", null, user);
    }
}
