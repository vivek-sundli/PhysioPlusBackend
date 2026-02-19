package com.healthclub.Physioplus.Repository;

import com.healthclub.Physioplus.Model.AppSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepository extends MongoRepository<AppSettings, String> {
    // Usually only one document with ID "GLOBAL"
}
