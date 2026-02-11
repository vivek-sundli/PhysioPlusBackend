package com.healthclub.Physioplus.config;

import com.healthclub.Physioplus.Repository.VideoSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackageClasses = VideoSessionRepository.class,
        mongoTemplateRef = "videoTemplate"
)
public class VideoMongoConfig {

    @Value("${spring.data.mongodb.video.uri}")
    private String videoUri;

    @Bean(name = "videoTemplate")
    public MongoTemplate videoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(videoUri));
    }
}
