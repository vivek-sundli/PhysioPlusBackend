package com.healthclub.Physioplus.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Unified MongoDB configuration for all repositories.
 * All repositories use the same MongoDB connection.
 */
@Configuration
@EnableMongoRepositories(
        basePackages = "com.healthclub.Physioplus.Repository",
        mongoTemplateRef = "mongoTemplate"
)
public class MongoConfig {

    @Value("${spring.data.mongodb.uri:${MONGODB_URI:mongodb://localhost:27017/PhysioPlus}}")
    private String mongoUri;

    @Primary
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Primary
    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        String databaseName = getDatabaseName(mongoUri);
        return new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
    }

    @Primary
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }

    private String getDatabaseName(String uri) {
        try {
            String dbName = new com.mongodb.ConnectionString(uri).getDatabase();
            return dbName != null ? dbName : "PhysioPlus";
        } catch (Exception e) {
            return "PhysioPlus";
        }
    }
}
