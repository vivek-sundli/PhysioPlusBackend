package com.healthclub.Physioplus.config;
// UPDATED package
 // UPDATED import
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configures the connection to the secondary "Payment" database.
 */
@Configuration
@EnableMongoRepositories(
        basePackages = "com.healthclub.Physioplus.Repository.PaymentRepository", // UPDATED to use your base package string
        mongoTemplateRef = "paymentTemplate" // Links to the "paymentTemplate" bean below
)
public class PaymentMongoConfig {

    @Value("${spring.data.mongodb.payment.uri}")
    private String paymentUri;

    @Bean(name = "paymentClient")
    public MongoClient paymentClient() {
        return MongoClients.create(paymentUri);
    }

    @Bean(name = "paymentFactory")
    public MongoDatabaseFactory paymentFactory(MongoClient paymentClient) {
        return new SimpleMongoClientDatabaseFactory(paymentClient, getDatabaseName(paymentUri));
    }

    @Bean(name = "paymentTemplate")
    public MongoTemplate paymentTemplate(MongoDatabaseFactory paymentFactory) {
        return new MongoTemplate(paymentFactory);
    }

    // Helper method to extract database name from the connection string
    private String getDatabaseName(String uri) {
        try {
            return new com.mongodb.ConnectionString(uri).getDatabase();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MongoDB URI: " + uri, e);
        }
    }
}

