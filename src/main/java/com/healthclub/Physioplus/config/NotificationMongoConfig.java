package com.healthclub.Physioplus.config;

import com.healthclub.Physioplus.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackageClasses = NotificationRepository.class,
        mongoTemplateRef = "notificationTemplate"
)
public class NotificationMongoConfig {

    @Value("${spring.data.mongodb.notification.uri}")
    private String notificationUri;

    @Bean(name = "notificationTemplate")
    public MongoTemplate notificationTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(notificationUri));
    }
}
