package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Dto.OtpType;
import com.healthclub.Physioplus.Model.OtpToken;
import com.healthclub.Physioplus.Repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    @Value("${otp.expiration-minutes}")
    private int otpExpirationMinutes;

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService, SmsService smsService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);  // 6-digit OTP
        return String.valueOf(otp);
    }

    public OtpToken createAndSendOtp(String userId, String target, OtpType type) {
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plus(otpExpirationMinutes, ChronoUnit.MINUTES);

        OtpToken otpToken = new OtpToken(userId, target, otp, type, expiresAt);
        otpToken.setCreatedAt(Instant.now());

        boolean sent = sendOtp(target, otp, type);
        if (!sent) {
            throw new RuntimeException("Failed to send OTP via " + type.name());
        }

        return otpTokenRepository.save(otpToken);
    }

    private boolean sendOtp(String target, String otp, OtpType type) {
        if (type == OtpType.EMAIL) {
            return emailService.sendOtp(target, otp);
        } else {
            return smsService.sendOtp(target, otp);
        }
    }

    public Optional<OtpToken> verifyOtp(String target, String otp) {
        Optional<OtpToken> tokenOpt = otpTokenRepository.findByTargetAndOtpAndUsedFalse(target, otp);

        if (tokenOpt.isPresent()) {
            OtpToken token = tokenOpt.get();
            if (token.isValid()) {
                token.setUsed(true);
                otpTokenRepository.save(token);
                return Optional.of(token);
            }
        }
        return Optional.empty();
    }

    public void invalidateOtpsForUser(String userId) {
        otpTokenRepository.deleteByUserId(userId);
    }

    public void invalidateOtpsForTarget(String target) {
        otpTokenRepository.deleteByTarget(target);
    }
}
