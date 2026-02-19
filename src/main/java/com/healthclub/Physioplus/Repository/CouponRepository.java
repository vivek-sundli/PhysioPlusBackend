package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {

    Optional<Coupon> findByCode(String code);

    List<Coupon> findByActiveTrue();

    @Query("{'active': true, 'validFrom': {$lte: ?0}, 'validUntil': {$gte: ?0}}")
    List<Coupon> findValidCoupons(Instant now);

    List<Coupon> findByTarget(Coupon.CouponTarget target);

    @Query("{'active': true, 'target': 'USER_SPECIFIC', 'targetUserIds': ?0}")
    List<Coupon> findCouponsForUser(String userId);

    @Query("{'active': true, 'target': 'DOCTOR_SPECIFIC', 'targetDoctorIds': ?0}")
    List<Coupon> findCouponsForDoctor(String doctorId);
}
