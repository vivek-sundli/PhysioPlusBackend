package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.LoyaltyAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends MongoRepository<LoyaltyAccount, String> {

    Optional<LoyaltyAccount> findByUserId(String userId);

    Optional<LoyaltyAccount> findByReferralCode(String referralCode);

    List<LoyaltyAccount> findByTier(LoyaltyAccount.LoyaltyTier tier);

    List<LoyaltyAccount> findByCurrentPointsGreaterThan(Integer points);
}
