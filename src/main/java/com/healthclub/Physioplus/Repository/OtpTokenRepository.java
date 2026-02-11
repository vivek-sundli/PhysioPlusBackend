package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Dto.OtpType;
import com.healthclub.Physioplus.Model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    Optional<OtpToken> findByTargetAndOtpAndUsedFalse(String target, String otp);

    Optional<OtpToken> findFirstByTargetAndTypeAndUsedFalseOrderByCreatedAtDesc(String target, OtpType type);

    void deleteByUserId(String userId);

    void deleteByTarget(String target);
}
