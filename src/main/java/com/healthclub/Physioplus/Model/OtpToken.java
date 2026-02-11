package com.healthclub.Physioplus.Model;

import com.healthclub.Physioplus.Dto.OtpType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "OtpTokens")
public class OtpToken {

    @Id
    private String id;

    private String userId;

    @Indexed
    private String target;  // email or phone number

    private String otp;

    private OtpType type;

    @Indexed
    private Instant expiresAt;

    private boolean used;

    @CreatedDate
    private Instant createdAt;

    public OtpToken() {
        this.used = false;
    }

    public OtpToken(String userId, String target, String otp, OtpType type, Instant expiresAt) {
        this();
        this.userId = userId;
        this.target = target;
        this.otp = otp;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
