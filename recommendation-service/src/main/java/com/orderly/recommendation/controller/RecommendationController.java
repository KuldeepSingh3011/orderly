package com.orderly.recommendation.controller;

import com.orderly.common.dto.ApiResponse;
import com.orderly.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Get personalized recommendations for a user.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecommendations(
            @PathVariable String userId) {

        List<String> recommendations = recommendationService.getRecommendations(userId);

        Map<String, Object> data = Map.of(
                "userId", userId,
                "recommendations", recommendations,
                "count", recommendations.size()
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Clear cached recommendations (useful when user makes new purchase).
     */
    @DeleteMapping("/{userId}/cache")
    public ResponseEntity<ApiResponse<Void>> clearCache(@PathVariable String userId) {
        recommendationService.clearCache(userId);
        return ResponseEntity.ok(ApiResponse.success("Cache cleared", null));
    }
}
