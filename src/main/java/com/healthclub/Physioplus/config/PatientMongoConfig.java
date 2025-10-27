package com.healthclub.Physioplus.config;


import com.healthclub.Physioplus.Repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration for the "Patient" database.
 * This class is responsible for setting up the MongoTemplate and
 * scanning *only* the PatientRepository.
 */
@Configuration
@EnableMongoRepositories(
        basePackages = "com.healthclub.Physioplus.Repository.PatientRepository", // CRITICAL: Links this config *only* to PatientRepository
        mongoTemplateRef = "patientTemplate" // Links to the "patientTemplate" bean below
)
public class PatientMongoConfig {

    // Reads the new property from application-*.properties
    @Value("${spring.data.mongodb.patient.uri}")
    private String patientUri;

    /**
     * Creates the MongoTemplate bean for the Patient database.
     */
    @Bean(name = "patientTemplate")
    public MongoTemplate patientTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(patientUri));
    }
}

