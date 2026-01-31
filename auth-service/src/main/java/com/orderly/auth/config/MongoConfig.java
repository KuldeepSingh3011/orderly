package com.orderly.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Bean
    public MongoConnectionDetails mongoConnectionDetails() {
        String mongoUri = getMongoConnectionString();
        String validatedUri = validateAndFixConnectionString(mongoUri);
        log.info("MongoDB connection string validated: {}", maskPassword(validatedUri));
        
        return new MongoConnectionDetails() {
            @Override
            public String getConnectionString() {
                return validatedUri;
            }
        };
    }

    private String getMongoConnectionString() {
        String uri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (uri != null && !uri.trim().isEmpty()) return uri.trim();
        uri = System.getenv("MONGO_URL");
        if (uri != null && !uri.trim().isEmpty()) return uri.trim();
        uri = System.getenv("DATABASE_URL");
        if (uri != null && !uri.trim().isEmpty()) return uri.trim();
        return "mongodb://localhost:27017/orderly";
    }

    private String validateAndFixConnectionString(String uri) {
        if (uri == null || uri.trim().isEmpty()) {
            throw new IllegalArgumentException("MongoDB URI is empty!");
        }
        String trimmed = uri.trim();
        if (trimmed.startsWith("mongodb://") || trimmed.startsWith("mongodb+srv://")) {
            return trimmed;
        }
        if (trimmed.contains(":") && !trimmed.contains("://")) {
            return "mongodb://" + trimmed;
        }
        if (!trimmed.contains("://")) {
            return "mongodb://" + trimmed + ":27017/orderly";
        }
        throw new IllegalArgumentException("Invalid MongoDB connection string: " + trimmed);
    }

    private String maskPassword(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            return "(empty)";
        }
        return connectionString.replaceAll("mongodb://([^:]+):([^@]+)@", "mongodb://$1:***@")
                             .replaceAll("mongodb\\+srv://([^:]+):([^@]+)@", "mongodb+srv://$1:***@");
    }
}
