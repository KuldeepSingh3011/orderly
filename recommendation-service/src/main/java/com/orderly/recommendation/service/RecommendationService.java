package com.orderly.recommendation.service;

import com.orderly.recommendation.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Generates personalized product recommendations using AI and caching.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final String CACHE_KEY_PREFIX = "recommendations:";

    private final AiService aiService;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public RecommendationService(
            AiService aiService,
            MongoTemplate mongoTemplate,
            RedisTemplate<String, Object> redisTemplate) {
        this.aiService = aiService;
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get recommendations for a user.
     * 1. Check Redis cache
     * 2. If miss, fetch order history from MongoDB
     * 3. Generate recommendations using AI
     * 4. Cache results for 1 hour
     */
    @SuppressWarnings("unchecked")
    public List<String> getRecommendations(String userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // Check cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for user {}", userId);
            return (List<String>) cached;
        }

        log.debug("Cache miss for user {}, generating recommendations", userId);

        // Fetch order history (simplified - just getting product names)
        List<String> purchaseHistory = fetchPurchaseHistory(userId);

        // Generate recommendations
        List<String> recommendations = aiService.generateRecommendations(userId, purchaseHistory);

        // Cache results
        redisTemplate.opsForValue().set(
                cacheKey,
                recommendations,
                RedisConfig.RECOMMENDATION_CACHE_TTL.toSeconds(),
                TimeUnit.SECONDS
        );

        return recommendations;
    }

    /**
     * Fetch purchase history from MongoDB.
     */
    private List<String> fetchPurchaseHistory(String userId) {
        // Query orders collection for this user's past purchases
        // Simplified: In real implementation, this would query the orders collection
        // and extract product names
        
        log.debug("Fetching purchase history for user {}", userId);
        
        // For now, return sample data if no orders exist
        return List.of(
                "Laptop",
                "Mouse",
                "Monitor"
        );
    }

    /**
     * Clear cached recommendations for a user.
     */
    public void clearCache(String userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.debug("Cleared recommendations cache for user {}", userId);
    }
}
