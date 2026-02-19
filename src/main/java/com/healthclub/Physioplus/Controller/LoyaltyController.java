package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.LoyaltyAccount;
import com.healthclub.Physioplus.Service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
@Tag(name = "Loyalty", description = "Loyalty program and rewards APIs")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @Autowired
    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    // ==================== Account Management ====================

    @PostMapping("/accounts")
    @Operation(summary = "Create loyalty account for user")
    public ResponseEntity<LoyaltyAccount> createAccount(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(loyaltyService.createAccount(request.get("userId")));
    }

    @GetMapping("/accounts/{userId}")
    @Operation(summary = "Get loyalty account by user ID")
    public ResponseEntity<LoyaltyAccount> getAccount(@PathVariable String userId) {
        return loyaltyService.getAccount(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/referral/{referralCode}")
    @Operation(summary = "Get account by referral code")
    public ResponseEntity<LoyaltyAccount> getAccountByReferralCode(@PathVariable String referralCode) {
        return loyaltyService.getAccountByReferralCode(referralCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/tier/{tier}")
    @Operation(summary = "Get all accounts by tier")
    public ResponseEntity<List<LoyaltyAccount>> getAccountsByTier(
            @PathVariable LoyaltyAccount.LoyaltyTier tier) {
        return ResponseEntity.ok(loyaltyService.getAccountsByTier(tier));
    }

    // ==================== Points Earning ====================

    @PostMapping("/earn/booking")
    @Operation(summary = "Earn points from booking")
    public ResponseEntity<LoyaltyAccount> earnFromBooking(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        double amount = ((Number) request.get("amount")).doubleValue();
        String bookingId = (String) request.get("bookingId");
        return ResponseEntity.ok(loyaltyService.earnPointsFromBooking(userId, amount, bookingId));
    }

    @PostMapping("/earn/referral")
    @Operation(summary = "Earn referral bonus")
    public ResponseEntity<LoyaltyAccount> earnReferralBonus(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(loyaltyService.earnReferralBonus(
                request.get("referrerId"),
                request.get("referredUserId")
        ));
    }

    @PostMapping("/earn/review")
    @Operation(summary = "Earn bonus for writing a review")
    public ResponseEntity<LoyaltyAccount> earnReviewBonus(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(loyaltyService.earnReviewBonus(
                request.get("userId"),
                request.get("reviewId")
        ));
    }

    @PostMapping("/earn/profile-complete")
    @Operation(summary = "Earn bonus for completing profile")
    public ResponseEntity<LoyaltyAccount> earnProfileCompleteBonus(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(loyaltyService.earnProfileCompleteBonus(request.get("userId")));
    }

    // ==================== Points Redemption ====================

    @PostMapping("/redeem")
    @Operation(summary = "Redeem points for discount")
    public ResponseEntity<LoyaltyAccount> redeemPoints(@RequestBody Map<String, Object> request) {
        String userId = (String) request.get("userId");
        int points = ((Number) request.get("points")).intValue();
        String description = (String) request.get("description");
        String referenceId = (String) request.get("referenceId");

        return loyaltyService.redeemPoints(userId, points, description, referenceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/redemption-value")
    @Operation(summary = "Calculate monetary value of points")
    public ResponseEntity<Map<String, Object>> calculateRedemptionValue(@RequestParam int points) {
        double value = loyaltyService.calculateRedemptionValue(points);
        return ResponseEntity.ok(Map.of(
                "points", points,
                "rupeesValue", value
        ));
    }

    @GetMapping("/points-for-amount")
    @Operation(summary = "Calculate points needed for a discount amount")
    public ResponseEntity<Map<String, Object>> calculatePointsForAmount(@RequestParam double amount) {
        int points = loyaltyService.calculatePointsForAmount(amount);
        return ResponseEntity.ok(Map.of(
                "amount", amount,
                "pointsRequired", points
        ));
    }

    // ==================== Delete ====================

    @DeleteMapping("/accounts/{userId}")
    @Operation(summary = "Delete loyalty account")
    public ResponseEntity<Void> deleteAccount(@PathVariable String userId) {
        if (loyaltyService.deleteAccount(userId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
