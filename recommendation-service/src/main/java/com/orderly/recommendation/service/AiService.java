package com.orderly.recommendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Ollama AI for generating recommendations.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final WebClient webClient;
    private final String model;
    private final int timeout;

    public AiService(
            @Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ai.ollama.model:llama2}") String model,
            @Value("${ai.ollama.timeout:30000}") int timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.model = model;
        this.timeout = timeout;
    }

    /**
     * Generate product recommendations using AI.
     * Falls back to simple recommendations if AI is unavailable.
     */
    public List<String> generateRecommendations(String userId, List<String> purchaseHistory) {
        try {
            String prompt = buildPrompt(purchaseHistory);
            String response = callOllama(prompt);
            return parseRecommendations(response);
        } catch (Exception e) {
            log.warn("AI service unavailable, using fallback recommendations: {}", e.getMessage());
            return getFallbackRecommendations();
        }
    }

    private String buildPrompt(List<String> purchaseHistory) {
        StringBuilder sb = new StringBuilder();
        sb.append("Based on the following purchase history, suggest 5 product recommendations. ");
        sb.append("Return only product names, one per line.\n\n");
        sb.append("Purchase history:\n");
        for (String item : purchaseHistory) {
            sb.append("- ").append(item).append("\n");
        }
        sb.append("\nRecommendations:");
        return sb.toString();
    }

    private String callOllama(String prompt) {
        Map<String, Object> request = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false
        );

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofMillis(timeout))
                .map(response -> (String) response.get("response"))
                .onErrorResume(e -> {
                    log.error("Error calling Ollama: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();
    }

    private List<String> parseRecommendations(String response) {
        if (response == null || response.isEmpty()) {
            return getFallbackRecommendations();
        }

        return response.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("-"))
                .limit(5)
                .toList();
    }

    private List<String> getFallbackRecommendations() {
        return List.of(
                "Wireless Bluetooth Headphones",
                "USB-C Fast Charger",
                "Laptop Stand",
                "Mechanical Keyboard",
                "Webcam HD 1080p"
        );
    }
}
