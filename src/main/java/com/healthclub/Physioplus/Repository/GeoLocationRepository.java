package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.GeoLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeoLocationRepository extends MongoRepository<GeoLocation, String> {

    Optional<GeoLocation> findByIpAddress(String ipAddress);
}
