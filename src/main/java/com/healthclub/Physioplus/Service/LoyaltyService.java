package com.healthclub.Physioplus.Service;

import com.healthclub.Physioplus.Model.LoyaltyAccount;
import com.healthclub.Physioplus.Repository.LoyaltyAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LoyaltyService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyService.class);

    // Tier thresholds
    private static final int SILVER_THRESHOLD = 500;
    private static final int GOLD_THRESHOLD = 2000;
    private static final int PLATINUM_THRESHOLD = 5000;

    // Points configuration
    private static final int POINTS_PER_RUPEE = 1;
    private static final int REFERRAL_BONUS = 100;
    private static final int REVIEW_BONUS = 25;
    private static final int PROFILE_COMPLETE_BONUS = 50;
    private static final int STREAK_BONUS = 10;

    private final LoyaltyAccountRepository loyaltyRepository;

    @Autowired
    public LoyaltyService(LoyaltyAccountRepository loyaltyRepository) {
        this.loyaltyRepository = loyaltyRepository;
    }

    // ==================== Account Management ====================

    public LoyaltyAccount createAccount(String userId) {
        Optional<LoyaltyAccount> existing = loyaltyRepository.findByUserId(userId);
        if (existing.isPresent()) {
            log.warn("Loyalty account already exists for user {}", userId);
            return existing.get();
        }

        LoyaltyAccount account = new LoyaltyAccount();
        account.setUserId(userId);
        account.setReferralCode(generateReferralCode());
        account.setCreatedAt(Instant.now());
        account.setUpdatedAt(Instant.now());

        log.info("Created loyalty account for user {}", userId);
        return loyaltyRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Optional<LoyaltyAccount> getAccount(String userId) {
        return loyaltyRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<LoyaltyAccount> getAccountByReferralCode(String referralCode) {
        return loyaltyRepository.findByReferralCode(referralCode);
    }

    // ==================== Points Earning ====================

    public LoyaltyAccount earnPointsFromBooking(String userId, double amount, String bookingId) {
        LoyaltyAccount account = getOrCreateAccount(userId);

        int basePoints = (int) (amount * POINTS_PER_RUPEE);
        int bonusPoints = (int) (basePoints * (account.getTier().getMultiplier() - 1));
        int totalPoints = basePoints + bonusPoints;

        addTransaction(account, LoyaltyAccount.PointTransaction.TransactionType.EARNED_BOOKING,
                totalPoints, "Booking points (base: " + basePoints + ", bonus: " + bonusPoints + ")", bookingId);

        account.setCurrentPoints(account.getCurrentPoints() + totalPoints);
        account.setLifetimePoints(account.getLifetimePoints() + totalPoints);
        account.setUpdatedAt(Instant.now());

        updateTier(account);
        log.info("User {} earned {} points from booking", userId, totalPoints);
        return loyaltyRepository.save(account);
    }

    public LoyaltyAccount earnReferralBonus(String referrerId, String referredUserId) {
        LoyaltyAccount referrerAccount = getOrCreateAccount(referrerId);

        addTransaction(referrerAccount, LoyaltyAccount.PointTransaction.TransactionType.EARNED_REFERRAL,
                REFERRAL_BONUS, "Referral bonus for user " + referredUserId, referredUserId);

        referrerAccount.setCurrentPoints(referrerAccount.getCurrentPoints() + REFERRAL_BONUS);
        referrerAccount.setLifetimePoints(referrerAccount.getLifetimePoints() + REFERRAL_BONUS);
        referrerAccount.setReferralCount(referrerAccount.getReferralCount() + 1);
        referrerAccount.setReferralPoints(referrerAccount.getReferralPoints() + REFERRAL_BONUS);
        referrerAccount.getReferredUserIds().add(referredUserId);
        referrerAccount.setUpdatedAt(Instant.now());

        updateTier(referrerAccount);
        log.info("User {} earned {} referral points", referrerId, REFERRAL_BONUS);
        return loyaltyRepository.save(referrerAccount);
    }

    public LoyaltyAccount earnReviewBonus(String userId, String reviewId) {
        LoyaltyAccount account = getOrCreateAccount(userId);

        addTransaction(account, LoyaltyAccount.PointTransaction.TransactionType.EARNED_REVIEW,
                REVIEW_BONUS, "Review bonus", reviewId);

        account.setCurrentPoints(account.getCurrentPoints() + REVIEW_BONUS);
        account.setLifetimePoints(account.getLifetimePoints() + REVIEW_BONUS);
        account.setUpdatedAt(Instant.now());

        updateTier(account);
        log.info("User {} earned {} review points", userId, REVIEW_BONUS);
        return loyaltyRepository.save(account);
    }

    public LoyaltyAccount earnProfileCompleteBonus(String userId) {
        LoyaltyAccount account = getOrCreateAccount(userId);

        addTransaction(account, LoyaltyAccount.PointTransaction.TransactionType.EARNED_PROFILE_COMPLETE,
                PROFILE_COMPLETE_BONUS, "Profile completion bonus", null);

        account.setCurrentPoints(account.getCurrentPoints() + PROFILE_COMPLETE_BONUS);
        account.setLifetimePoints(account.getLifetimePoints() + PROFILE_COMPLETE_BONUS);
        account.setUpdatedAt(Instant.now());

        updateTier(account);
        log.info("User {} earned {} profile completion points", userId, PROFILE_COMPLETE_BONUS);
        return loyaltyRepository.save(account);
    }

    // ==================== Points Redemption ====================

    public Optional<LoyaltyAccount> redeemPoints(String userId, int points, String description, String referenceId) {
        return loyaltyRepository.findByUserId(userId)
                .filter(account -> account.getCurrentPoints() >= points)
                .map(account -> {
                    addTransaction(account, LoyaltyAccount.PointTransaction.TransactionType.REDEEMED_DISCOUNT,
                            -points, description, referenceId);

                    account.setCurrentPoints(account.getCurrentPoints() - points);
                    account.setRedeemedPoints(account.getRedeemedPoints() + points);
                    account.setUpdatedAt(Instant.now());

                    log.info("User {} redeemed {} points", userId, points);
                    return loyaltyRepository.save(account);
                });
    }

    public double calculateRedemptionValue(int points) {
        // 4 points = 1 rupee (0.25 rupees per point)
        return points * 0.25;
    }

    public int calculatePointsForAmount(double amount) {
        // How many points needed for this amount discount
        return (int) (amount / 0.25);
    }

    // ==================== Tier Management ====================

    private void updateTier(LoyaltyAccount account) {
        int lifetime = account.getLifetimePoints();
        LoyaltyAccount.LoyaltyTier newTier;

        if (lifetime >= PLATINUM_THRESHOLD) {
            newTier = LoyaltyAccount.LoyaltyTier.PLATINUM;
            account.setPointsToNextTier(0);
        } else if (lifetime >= GOLD_THRESHOLD) {
            newTier = LoyaltyAccount.LoyaltyTier.GOLD;
            account.setPointsToNextTier(PLATINUM_THRESHOLD - lifetime);
        } else if (lifetime >= SILVER_THRESHOLD) {
            newTier = LoyaltyAccount.LoyaltyTier.SILVER;
            account.setPointsToNextTier(GOLD_THRESHOLD - lifetime);
        } else {
            newTier = LoyaltyAccount.LoyaltyTier.BRONZE;
            account.setPointsToNextTier(SILVER_THRESHOLD - lifetime);
        }

        if (account.getTier() != newTier) {
            log.info("User {} upgraded from {} to {}", account.getUserId(), account.getTier(), newTier);
            account.setTier(newTier);
        }
    }

    @Transactional(readOnly = true)
    public List<LoyaltyAccount> getAccountsByTier(LoyaltyAccount.LoyaltyTier tier) {
        return loyaltyRepository.findByTier(tier);
    }

    // ==================== Transaction History ====================

    private void addTransaction(LoyaltyAccount account,
                               LoyaltyAccount.PointTransaction.TransactionType type,
                               int points, String description, String referenceId) {
        LoyaltyAccount.PointTransaction transaction = new LoyaltyAccount.PointTransaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setType(type);
        transaction.setPoints(points);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setTimestamp(Instant.now());

        account.getTransactions().add(transaction);
    }

    // ==================== Utilities ====================

    private LoyaltyAccount getOrCreateAccount(String userId) {
        return loyaltyRepository.findByUserId(userId)
                .orElseGet(() -> createAccount(userId));
    }

    private String generateReferralCode() {
        return "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public boolean deleteAccount(String userId) {
        return loyaltyRepository.findByUserId(userId)
                .map(account -> {
                    loyaltyRepository.delete(account);
                    log.info("Deleted loyalty account for user {}", userId);
                    return true;
                })
                .orElse(false);
    }
}
