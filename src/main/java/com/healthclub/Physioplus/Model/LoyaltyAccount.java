package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "LoyaltyAccounts")
public class LoyaltyAccount {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    // Points
    private Integer currentPoints;
    private Integer lifetimePoints;
    private Integer redeemedPoints;

    // Tier
    private LoyaltyTier tier;
    private Integer pointsToNextTier;
    private Instant tierExpiresAt;

    // Referral
    private String referralCode;
    private Integer referralCount;
    private Integer referralPoints;
    private List<String> referredUserIds;

    // History
    private List<PointTransaction> transactions;

    // Rewards
    private List<String> unlockedRewardIds;
    private List<String> claimedCouponIds;

    private Instant createdAt;
    private Instant updatedAt;

    public LoyaltyAccount() {
        this.currentPoints = 0;
        this.lifetimePoints = 0;
        this.redeemedPoints = 0;
        this.tier = LoyaltyTier.BRONZE;
        this.referralCount = 0;
        this.referralPoints = 0;
        this.transactions = new ArrayList<>();
        this.referredUserIds = new ArrayList<>();
        this.unlockedRewardIds = new ArrayList<>();
        this.claimedCouponIds = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public enum LoyaltyTier {
        BRONZE(0, 1.0),
        SILVER(500, 1.25),
        GOLD(2000, 1.5),
        PLATINUM(5000, 2.0);

        private final int minPoints;
        private final double multiplier;

        LoyaltyTier(int minPoints, double multiplier) {
            this.minPoints = minPoints;
            this.multiplier = multiplier;
        }

        public int getMinPoints() { return minPoints; }
        public double getMultiplier() { return multiplier; }
    }

    @Getter
    @Setter
    public static class PointTransaction {
        private String id;
        private TransactionType type;
        private Integer points;
        private String description;
        private String referenceId;   // bookingId, paymentId, etc.
        private Instant timestamp;

        public enum TransactionType {
            EARNED_BOOKING,
            EARNED_REFERRAL,
            EARNED_REVIEW,
            EARNED_PROFILE_COMPLETE,
            EARNED_STREAK,
            REDEEMED_DISCOUNT,
            REDEEMED_REWARD,
            EXPIRED,
            ADJUSTED
        }
    }
}
