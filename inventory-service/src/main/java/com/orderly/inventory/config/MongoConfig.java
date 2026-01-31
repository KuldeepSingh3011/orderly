package com.orderly.inventory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

/**
 * MongoDB configuration that handles Railway's connection strings properly.
 * Validates and fixes connection strings before Spring Boot uses them.
 */
@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public MongoConnectionDetails mongoConnectionDetails() {
        // Get connection string from environment variables (Railway) or use default
        String mongoUri = getMongoConnectionString();
        
        log.info("MongoDB Configuration:");
        log.info("  Connection String: {}", maskPassword(mongoUri));
        log.info("  Environment Variables:");
        log.info("    SPRING_DATA_MONGODB_URI: {}", getEnvValue("SPRING_DATA_MONGODB_URI"));
        log.info("    MONGO_URL: {}", getEnvValue("MONGO_URL"));
        log.info("    DATABASE_URL: {}", getEnvValue("DATABASE_URL"));
        
        // Validate and fix connection string
        String validatedUri = validateAndFixConnectionString(mongoUri);
        
        log.info("MongoDB connection string validated: {}", maskPassword(validatedUri));
        
        return new MongoConnectionDetails() {
            @Override
            public String getConnectionString() {
                return validatedUri;
            }
        };
    }

    /**
     * Gets MongoDB connection string from environment variables with fallback.
     */
    private String getMongoConnectionString() {
        // Check SPRING_DATA_MONGODB_URI first
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri != null && !uri.trim().isEmpty()) {
            return uri.trim();
        }
        
        // Check MONGO_URL (Railway)
        uri = System.getenv("MONGO_URL");
        if (uri != null && !uri.trim().isEmpty()) {
            return uri.trim();
        }
        
        // Check DATABASE_URL
        uri = System.getenv("DATABASE_URL");
        if (uri != null && !uri.trim().isEmpty()) {
            return uri.trim();
        }
        
        // Default for local development
        return "mongodb://localhost:27017/orderly";
    }

    /**
     * Validates and fixes MongoDB connection string.
     */
    private String validateAndFixConnectionString(String uri) {
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("MongoDB URI is empty! Please set SPRING_DATA_MONGODB_URI, MONGO_URL, or DATABASE_URL");
        }
        
        String trimmed = uri.trim();
        
        // If already valid, return as-is
        if (trimmed.startsWith("mongodb://") || trimmed.startsWith("mongodb+srv://")) {
            return trimmed;
        }
        
        // Try to fix common issues
        // If it's just a host:port, add mongodb:// prefix
        if (trimmed.contains(":") && !trimmed.contains("://")) {
            String fixed = "mongodb://" + trimmed;
            log.warn("MongoDB URI missing prefix. Fixed: {} -> {}", trimmed, maskPassword(fixed));
            return fixed;
        }
        
        // If it's just a hostname, add mongodb:// prefix and default port
        if (!trimmed.contains("://") && !trimmed.contains(":")) {
            String fixed = "mongodb://" + trimmed + ":27017/orderly";
            log.warn("MongoDB URI missing prefix and port. Fixed: {} -> {}", trimmed, maskPassword(fixed));
            return fixed;
        }
        
        // Can't fix it
        throw new IllegalArgumentException(
            String.format("Invalid MongoDB connection string: '%s'. Must start with 'mongodb://' or 'mongodb+srv://'. " +
                "If using Railway, ensure MONGO_URL is set correctly from your MongoDB service.", trimmed)
        );
    }

    /**
     * Gets environment variable value for logging.
     */
    private String getEnvValue(String key) {
        String value = System.getenv(key);
        if (value == null) {
            return "NOT SET";
        }
        if (value.isEmpty()) {
            return "SET (empty)";
        }
        return "SET: " + maskPassword(value);
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
