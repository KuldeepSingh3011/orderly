package com.orderly.recommendation.service;

import com.orderly.recommendation.config.RedisConfig;
import com.orderly.recommendation.dto.ProductDTO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Generates personalized product recommendations based on order history and available inventory.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final String CACHE_KEY_PREFIX = "recommendations:";
    private static final int MAX_RECOMMENDATIONS = 5;

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public RecommendationService(
            MongoTemplate mongoTemplate,
            RedisTemplate<String, Object> redisTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get product recommendations for a user.
     * 1. Check Redis cache
     * 2. If miss, fetch order history and generate recommendations
     * 3. Cache results for 1 hour
     */
    @SuppressWarnings("unchecked")
    public List<ProductDTO> getRecommendations(String userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // Check cache
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && cached instanceof List) {
                log.debug("Cache hit for user {}", userId);
                return (List<ProductDTO>) cached;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed: {}", e.getMessage());
        }

        log.debug("Cache miss for user {}, generating recommendations", userId);

        // Fetch user's purchase history
        Set<String> purchasedProductIds = fetchPurchasedProductIds(userId);
        Set<String> purchasedCategories = fetchPurchasedCategories(userId, purchasedProductIds);

        // Generate recommendations based on purchase history
        List<ProductDTO> recommendations = generateRecommendations(purchasedProductIds, purchasedCategories);

        // Cache results
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    recommendations,
                    RedisConfig.RECOMMENDATION_CACHE_TTL.toSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("Redis cache write failed: {}", e.getMessage());
        }

        return recommendations;
    }

    /**
     * Fetch product IDs from user's order history.
     */
    private Set<String> fetchPurchasedProductIds(String userId) {
        try {
            Query query = new Query(Criteria.where("userId").is(userId));
            List<Document> orders = mongoTemplate.find(query, Document.class, "orders");
            
            Set<String> productIds = new HashSet<>();
            for (Document order : orders) {
                List<Document> items = order.getList("items", Document.class);
                if (items != null) {
                    for (Document item : items) {
                        String productId = item.getString("productId");
                        if (productId != null) {
                            productIds.add(productId);
                        }
                    }
                }
            }
            log.debug("Found {} purchased products for user {}", productIds.size(), userId);
            return productIds;
        } catch (Exception e) {
            log.warn("Failed to fetch purchase history: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Fetch categories of purchased products.
     */
    private Set<String> fetchPurchasedCategories(String userId, Set<String> purchasedProductIds) {
        if (purchasedProductIds.isEmpty()) {
            return Collections.emptySet();
        }

        try {
            Query query = new Query(Criteria.where("_id").in(purchasedProductIds));
            List<Document> products = mongoTemplate.find(query, Document.class, "products");
            
            return products.stream()
                    .map(p -> p.getString("category"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("Failed to fetch categories: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Generate product recommendations.
     * Strategy:
     * 1. If user has purchase history, recommend products from similar categories they haven't bought
     * 2. If no history, recommend popular/random products from inventory
     */
    private List<ProductDTO> generateRecommendations(Set<String> purchasedProductIds, Set<String> purchasedCategories) {
        List<ProductDTO> recommendations = new ArrayList<>();

        // Get products from similar categories that user hasn't purchased
        if (!purchasedCategories.isEmpty()) {
            recommendations.addAll(getProductsByCategories(purchasedCategories, purchasedProductIds));
        }

        // If not enough recommendations, add other available products
        if (recommendations.size() < MAX_RECOMMENDATIONS) {
            Set<String> excludeIds = new HashSet<>(purchasedProductIds);
            excludeIds.addAll(recommendations.stream().map(ProductDTO::getId).collect(Collectors.toSet()));
            
            List<ProductDTO> moreProducts = getRandomAvailableProducts(excludeIds, MAX_RECOMMENDATIONS - recommendations.size());
            recommendations.addAll(moreProducts);
        }

        return recommendations.stream()
                .limit(MAX_RECOMMENDATIONS)
                .collect(Collectors.toList());
    }

    /**
     * Get products from specified categories, excluding already purchased ones.
     */
    private List<ProductDTO> getProductsByCategories(Set<String> categories, Set<String> excludeIds) {
        try {
            Criteria criteria = Criteria.where("category").in(categories)
                    .and("active").is(true)
                    .and("stockQuantity").gt(0);
            
            if (!excludeIds.isEmpty()) {
                criteria = criteria.and("_id").nin(excludeIds);
            }

            Query query = new Query(criteria).limit(MAX_RECOMMENDATIONS);
            List<Document> products = mongoTemplate.find(query, Document.class, "products");
            
            return products.stream()
                    .map(this::documentToProductDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to fetch products by category: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get random available products.
     */
    private List<ProductDTO> getRandomAvailableProducts(Set<String> excludeIds, int limit) {
        try {
            Criteria criteria = Criteria.where("active").is(true)
                    .and("stockQuantity").gt(0);
            
            if (!excludeIds.isEmpty()) {
                criteria = criteria.and("_id").nin(excludeIds);
            }

            Query query = new Query(criteria).limit(limit);
            List<Document> products = mongoTemplate.find(query, Document.class, "products");
            
            // Shuffle for randomness
            Collections.shuffle(products);
            
            return products.stream()
                    .limit(limit)
                    .map(this::documentToProductDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to fetch random products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Convert MongoDB Document to ProductDTO.
     */
    private ProductDTO documentToProductDTO(Document doc) {
        ProductDTO dto = new ProductDTO();
        dto.setId(doc.getObjectId("_id").toString());
        dto.setSku(doc.getString("sku"));
        dto.setName(doc.getString("name"));
        dto.setDescription(doc.getString("description"));
        dto.setCategory(doc.getString("category"));
        
        Object priceObj = doc.get("price");
        if (priceObj instanceof Number) {
            dto.setPrice(new BigDecimal(priceObj.toString()));
        }
        
        Integer stockQty = doc.getInteger("stockQuantity", 0);
        Integer reservedQty = doc.getInteger("reservedQuantity", 0);
        dto.setStockQuantity(stockQty);
        dto.setAvailableQuantity(stockQty - reservedQty);
        dto.setImageUrl(doc.getString("imageUrl"));
        
        return dto;
    }

    /**
     * Clear cached recommendations for a user.
     */
    public void clearCache(String userId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + userId;
            redisTemplate.delete(cacheKey);
            log.debug("Cleared recommendations cache for user {}", userId);
        } catch (Exception e) {
            log.warn("Failed to clear cache: {}", e.getMessage());
        }
    }
}
