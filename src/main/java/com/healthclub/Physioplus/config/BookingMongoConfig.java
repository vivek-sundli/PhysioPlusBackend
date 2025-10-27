package com.healthclub.Physioplus.config;

import com.healthclub.Physioplus.Repository.BookingRepository;
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
 * Configures the connection to the primary "Booking" database.
 */
@Configuration
@EnableMongoRepositories(
        basePackageClasses = BookingRepository.class, // UPDATED: More specific than scanning the whole package
        mongoTemplateRef = "bookingTemplate" // Links to the "bookingTemplate" bean below
)
public class BookingMongoConfig {

    @Value("${spring.data.mongodb.booking.uri}")
    private String bookingUri;

    @Primary // Mark this as the default connection
    @Bean(name = "bookingClient")
    public MongoClient bookingClient() {
        return MongoClients.create(bookingUri);
    }

    @Primary
    @Bean(name = "bookingFactory")
    public MongoDatabaseFactory bookingFactory(MongoClient bookingClient) {
        // The factory needs to know the database name, which is part of the URI.
        // SimpleMongoClientDatabaseFactory correctly parses it from the full URI string.
        return new SimpleMongoClientDatabaseFactory(bookingClient, getDatabaseName(bookingUri));
    }

    @Primary
    @Bean(name = "bookingTemplate")
    public MongoTemplate bookingTemplate(MongoDatabaseFactory bookingFactory) {
        return new MongoTemplate(bookingFactory);
    }

    // Helper method to extract database name from the connection string
    private String getDatabaseName(String uri) {
        // A simple parser for a MongoDB Atlas URI
        // mongodb+srv://.../.../DATABASE_NAME?options
        try {
            return new com.mongodb.ConnectionString(uri).getDatabase();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid MongoDB URI: " + uri, e);
        }
    }
}


