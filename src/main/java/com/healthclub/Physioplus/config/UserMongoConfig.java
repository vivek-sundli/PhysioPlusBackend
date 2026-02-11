package com.healthclub.Physioplus.config;

import com.healthclub.Physioplus.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackageClasses = UserRepository.class,
        mongoTemplateRef = "userTemplate"
)
public class UserMongoConfig {

    @Value("${spring.data.mongodb.user.uri}")
    private String userUri;

    @Bean(name = "userTemplate")
    public MongoTemplate userTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(userUri));
    }
}
