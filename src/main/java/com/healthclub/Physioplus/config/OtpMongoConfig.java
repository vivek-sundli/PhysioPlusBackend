package com.healthclub.Physioplus.config;

import com.healthclub.Physioplus.Repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackageClasses = OtpTokenRepository.class,
        mongoTemplateRef = "otpTemplate"
)
public class OtpMongoConfig {

    @Value("${spring.data.mongodb.user.uri}")
    private String otpUri;

    @Bean(name = "otpTemplate")
    public MongoTemplate otpTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(otpUri));
    }
}
