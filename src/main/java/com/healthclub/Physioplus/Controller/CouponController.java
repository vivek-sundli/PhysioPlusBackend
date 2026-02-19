package com.healthclub.Physioplus.Controller;

import com.healthclub.Physioplus.Model.Coupon;
import com.healthclub.Physioplus.Service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@Tag(name = "Coupons", description = "Coupon and discount management APIs")
public class CouponController {

    private final CouponService couponService;

    @Autowired
    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // ==================== CRUD ====================

    @PostMapping
    @Operation(summary = "Create a new coupon (admin)")
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID")
    public ResponseEntity<Coupon> getCouponById(@PathVariable String id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active coupons (admin)")
    public ResponseEntity<List<Coupon>> getAllActiveCoupons() {
        return ResponseEntity.ok(couponService.getAllActiveCoupons());
    }

    @GetMapping("/valid")
    @Operation(summary = "Get all currently valid coupons")
    public ResponseEntity<List<Coupon>> getValidCoupons() {
        return ResponseEntity.ok(couponService.getValidCoupons());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update coupon (admin)")
    public ResponseEntity<Coupon> updateCoupon(
            @PathVariable String id,
            @Valid @RequestBody Coupon coupon) {
        return couponService.updateCoupon(id, coupon)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a coupon (admin)")
    public ResponseEntity<Coupon> deactivateCoupon(@PathVariable String id) {
        return couponService.deactivateCoupon(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Validation ====================

    @PostMapping("/validate")
    @Operation(summary = "Validate a coupon code")
    public ResponseEntity<Map<String, Object>> validateCoupon(@RequestBody Map<String, Object> request) {
        String code = (String) request.get("code");
        String userId = (String) request.get("userId");
        String doctorId = (String) request.get("doctorId");
        double orderAmount = ((Number) request.get("orderAmount")).doubleValue();
        String serviceType = (String) request.get("serviceType");
        boolean isFirstBooking = (boolean) request.getOrDefault("isFirstBooking", false);

        CouponService.CouponValidationResult result = couponService.validateCoupon(
                code, userId, doctorId, orderAmount, serviceType, isFirstBooking);

        return ResponseEntity.ok(Map.of(
                "valid", result.isValid(),
                "message", result.getMessage(),
                "discountAmount", result.getDiscountAmount(),
                "coupon", result.getCoupon() != null ? result.getCoupon() : Map.of()
        ));
    }

    // ==================== Apply ====================

    @PostMapping("/{couponId}/apply")
    @Operation(summary = "Apply coupon (increment usage count)")
    public ResponseEntity<Coupon> applyCoupon(@PathVariable String couponId) {
        return ResponseEntity.ok(couponService.applyCoupon(couponId));
    }

    // ==================== User Coupons ====================

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get available coupons for a user")
    public ResponseEntity<List<Coupon>> getCouponsForUser(
            @PathVariable String userId,
            @RequestParam(required = false) String doctorId) {
        return ResponseEntity.ok(couponService.getCouponsForUser(userId, doctorId));
    }
}
