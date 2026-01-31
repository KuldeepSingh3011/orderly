package com.orderly.inventory.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * MongoDB configuration with connection string validation and logging.
 * Validates the connection string before Spring Boot tries to connect.
 * Handles Railway's MongoDB connection strings properly.
 */
@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @PostConstruct
    public void validateConnectionString() {
        String connectionString = mongoUri != null ? mongoUri.trim() : "";
        
        log.info("MongoDB Configuration:");
        log.info("  Connection String: {}", maskPassword(connectionString));
        log.info("  Environment Variables:");
        log.info("    SPRING_DATA_MONGODB_URI: {}", System.getenv("SPRING_DATA_MONGODB_URI") != null ? "SET" : "NOT SET");
        log.info("    MONGO_URL: {}", System.getenv("MONGO_URL") != null ? "SET" : "NOT SET");
        log.info("    DATABASE_URL: {}", System.getenv("DATABASE_URL") != null ? "SET" : "NOT SET");
        
        if (connectionString.isEmpty()) {
            String error = "MongoDB URI is empty! Please set one of: SPRING_DATA_MONGODB_URI, MONGO_URL, or DATABASE_URL";
            log.error(error);
            throw new IllegalArgumentException(error);
        }
        
        if (!connectionString.startsWith("mongodb://") && !connectionString.startsWith("mongodb+srv://")) {
            String error = String.format(
                "Invalid MongoDB connection string: '%s'. Must start with 'mongodb://' or 'mongodb+srv://'. " +
                "If using Railway, ensure MONGO_URL is set correctly from your MongoDB service.",
                connectionString
            );
            log.error(error);
            throw new IllegalArgumentException(error);
        }
        
        log.info("MongoDB connection string validated successfully");
    }

    /**
     * Masks password in connection string for logging.
     */
    private String maskPassword(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            return "(empty)";
        }
        // Mask password in mongodb://user:password@host format
        return connectionString.replaceAll("mongodb://([^:]+):([^@]+)@", "mongodb://$1:***@")
                             .replaceAll("mongodb\\+srv://([^:]+):([^@]+)@", "mongodb+srv://$1:***@");
    }
}
