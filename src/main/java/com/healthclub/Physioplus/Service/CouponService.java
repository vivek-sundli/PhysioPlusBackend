package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Model.Coupon;
import com.healthclub.Physioplus.Model.LoyaltyAccount;
import com.healthclub.Physioplus.Repository.CouponRepository;
import com.healthclub.Physioplus.Repository.LoyaltyAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final CouponRepository couponRepository;
    private final LoyaltyAccountRepository loyaltyRepository;

    @Autowired
    public CouponService(CouponRepository couponRepository,
                         LoyaltyAccountRepository loyaltyRepository) {
        this.couponRepository = couponRepository;
        this.loyaltyRepository = loyaltyRepository;
    }

    // ==================== Coupon CRUD ====================

    public Coupon createCoupon(Coupon coupon) {
        // Validate code uniqueness
        if (couponRepository.findByCode(coupon.getCode()).isPresent()) {
            throw new RuntimeException("Coupon code already exists: " + coupon.getCode());
        }

        coupon.setActive(true);
        coupon.setUsedCount(0);
        coupon.setCreatedAt(Instant.now());

        log.info("Creating coupon: {}", coupon.getCode());
        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> getCouponByCode(String code) {
        return couponRepository.findByCode(code);
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> getCouponById(String id) {
        return couponRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Coupon> getAllActiveCoupons() {
        return couponRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Coupon> getValidCoupons() {
        return couponRepository.findValidCoupons(Instant.now());
    }

    public Optional<Coupon> updateCoupon(String id, Coupon updated) {
        return couponRepository.findById(id)
                .map(coupon -> {
                    coupon.setName(updated.getName());
                    coupon.setDescription(updated.getDescription());
                    coupon.setDiscountType(updated.getDiscountType());
                    coupon.setDiscountValue(updated.getDiscountValue());
                    coupon.setMaxDiscountAmount(updated.getMaxDiscountAmount());
                    coupon.setMinOrderAmount(updated.getMinOrderAmount());
                    coupon.setValidFrom(updated.getValidFrom());
                    coupon.setValidUntil(updated.getValidUntil());
                    coupon.setTotalUsageLimit(updated.getTotalUsageLimit());
                    coupon.setPerUserLimit(updated.getPerUserLimit());
                    coupon.setTarget(updated.getTarget());
                    coupon.setTargetUserIds(updated.getTargetUserIds());
                    coupon.setTargetDoctorIds(updated.getTargetDoctorIds());
                    coupon.setMinTier(updated.getMinTier());
                    coupon.setApplicableServices(updated.getApplicableServices());
                    coupon.setFirstTimeOnly(updated.isFirstTimeOnly());
                    log.info("Updated coupon: {}", coupon.getCode());
                    return couponRepository.save(coupon);
                });
    }

    public Optional<Coupon> deactivateCoupon(String id) {
        return couponRepository.findById(id)
                .map(coupon -> {
                    coupon.setActive(false);
                    log.info("Deactivated coupon: {}", coupon.getCode());
                    return couponRepository.save(coupon);
                });
    }

    // ==================== Coupon Validation ====================

    public CouponValidationResult validateCoupon(String code, String userId, String doctorId,
                                                   double orderAmount, String serviceType,
                                                   boolean isFirstBooking) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isEmpty()) {
            return new CouponValidationResult(false, "Invalid coupon code", null, 0);
        }

        Coupon coupon = couponOpt.get();

        // Check if active
        if (!coupon.isActive()) {
            return new CouponValidationResult(false, "Coupon is no longer active", null, 0);
        }

        // Check validity period
        Instant now = Instant.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return new CouponValidationResult(false, "Coupon is not yet valid", null, 0);
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return new CouponValidationResult(false, "Coupon has expired", null, 0);
        }

        // Check usage limits
        if (coupon.getTotalUsageLimit() != null && coupon.getUsedCount() >= coupon.getTotalUsageLimit()) {
            return new CouponValidationResult(false, "Coupon usage limit reached", null, 0);
        }

        // Check minimum order amount
        if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
            return new CouponValidationResult(false,
                    "Minimum order amount is ₹" + coupon.getMinOrderAmount(), null, 0);
        }

        // Check first-time only
        if (coupon.isFirstTimeOnly() && !isFirstBooking) {
            return new CouponValidationResult(false, "Coupon is for first-time users only", null, 0);
        }

        // Check service applicability
        if (coupon.getApplicableServices() != null && !coupon.getApplicableServices().isEmpty()) {
            if (!coupon.getApplicableServices().contains(serviceType)) {
                return new CouponValidationResult(false,
                        "Coupon not applicable for this service", null, 0);
            }
        }

        // Check target restrictions
        if (!checkTargetEligibility(coupon, userId, doctorId)) {
            return new CouponValidationResult(false, "You are not eligible for this coupon", null, 0);
        }

        // Check loyalty tier
        if (coupon.getMinTier() != null) {
            Optional<LoyaltyAccount> loyaltyOpt = loyaltyRepository.findByUserId(userId);
            if (loyaltyOpt.isEmpty() ||
                loyaltyOpt.get().getTier().ordinal() < coupon.getMinTier().ordinal()) {
                return new CouponValidationResult(false,
                        "Requires " + coupon.getMinTier() + " tier or higher", null, 0);
            }
        }

        // Calculate discount
        double discount = calculateDiscount(coupon, orderAmount);

        return new CouponValidationResult(true, "Coupon applied successfully", coupon, discount);
    }

    private boolean checkTargetEligibility(Coupon coupon, String userId, String doctorId) {
        if (coupon.getTarget() == null || coupon.getTarget() == Coupon.CouponTarget.ALL_USERS) {
            return true;
        }

        return switch (coupon.getTarget()) {
            case USER_SPECIFIC -> coupon.getTargetUserIds() != null &&
                                  coupon.getTargetUserIds().contains(userId);
            case DOCTOR_SPECIFIC -> coupon.getTargetDoctorIds() != null &&
                                    coupon.getTargetDoctorIds().contains(doctorId);
            case LOYALTY_TIER -> true; // Handled separately
            case NEW_USERS -> true; // Would need booking history check
            default -> true;
        };
    }

    private double calculateDiscount(Coupon coupon, double orderAmount) {
        double discount = 0;

        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount * (coupon.getDiscountValue() / 100);
                if (coupon.getMaxDiscountAmount() != null) {
                    discount = Math.min(discount, coupon.getMaxDiscountAmount());
                }
                break;
            case FIXED_AMOUNT:
                discount = coupon.getDiscountValue();
                break;
            case FREE_CONSULTATION:
                discount = orderAmount; // Full discount
                break;
            case CASHBACK:
                // Cashback is given after payment, not as discount
                discount = 0;
                break;
        }

        return Math.min(discount, orderAmount); // Can't exceed order amount
    }

    // ==================== Coupon Usage ====================

    public Coupon applyCoupon(String couponId) {
        return couponRepository.findById(couponId)
                .map(coupon -> {
                    coupon.setUsedCount(coupon.getUsedCount() + 1);
                    log.info("Coupon {} applied. Used count: {}", coupon.getCode(), coupon.getUsedCount());
                    return couponRepository.save(coupon);
                })
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    // ==================== Get Coupons for User ====================

    @Transactional(readOnly = true)
    public List<Coupon> getCouponsForUser(String userId, String doctorId) {
        List<Coupon> allCoupons = couponRepository.findValidCoupons(Instant.now());

        return allCoupons.stream()
                .filter(coupon -> checkTargetEligibility(coupon, userId, doctorId))
                .collect(Collectors.toList());
    }

    // ==================== Result Class ====================

    public static class CouponValidationResult {
        private final boolean valid;
        private final String message;
        private final Coupon coupon;
        private final double discountAmount;

        public CouponValidationResult(boolean valid, String message, Coupon coupon, double discountAmount) {
            this.valid = valid;
            this.message = message;
            this.coupon = coupon;
            this.discountAmount = discountAmount;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public Coupon getCoupon() { return coupon; }
        public double getDiscountAmount() { return discountAmount; }
    }
}
