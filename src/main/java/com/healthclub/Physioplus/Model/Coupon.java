package com.healthclub.Physioplus.Model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Document(collection = "Coupons")
public class Coupon {

    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private String name;
    private String description;

    // Discount Type
    private DiscountType discountType;
    private Double discountValue;      // Percentage or fixed amount
    private Double maxDiscountAmount;  // Cap for percentage discounts
    private Double minOrderAmount;     // Minimum order to apply

    // Validity
    private Instant validFrom;
    private Instant validUntil;
    private boolean active;

    // Usage Limits
    private Integer totalUsageLimit;
    private Integer usedCount;
    private Integer perUserLimit;

    // Targeting
    private CouponTarget target;
    private List<String> targetUserIds;      // For USER_SPECIFIC
    private List<String> targetDoctorIds;    // For DOCTOR_SPECIFIC
    private LoyaltyAccount.LoyaltyTier minTier;  // Minimum loyalty tier

    // Applicability
    private List<String> applicableServices; // CONSULTATION, VIDEO_CALL, etc.
    private boolean firstTimeOnly;
    private boolean referralCoupon;

    private Instant createdAt;
    private String createdBy;

    public Coupon() {
        this.active = true;
        this.usedCount = 0;
        this.firstTimeOnly = false;
        this.referralCoupon = false;
        this.createdAt = Instant.now();
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT,
        FREE_CONSULTATION,
        CASHBACK
    }

    public enum CouponTarget {
        ALL_USERS,
        NEW_USERS,
        LOYALTY_TIER,
        USER_SPECIFIC,
        DOCTOR_SPECIFIC
    }
}
