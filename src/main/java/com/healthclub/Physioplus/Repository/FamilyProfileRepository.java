package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.FamilyProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyProfileRepository extends MongoRepository<FamilyProfile, String> {

    Optional<FamilyProfile> findByPrimaryUserId(String primaryUserId);
}
