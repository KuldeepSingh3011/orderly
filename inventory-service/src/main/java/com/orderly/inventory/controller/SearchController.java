package com.orderly.inventory.controller;

import com.orderly.common.dto.ApiResponse;
import com.orderly.inventory.entity.Product;
import com.orderly.inventory.search.ProductDocument;
import com.orderly.inventory.search.ProductSearchService;
import com.orderly.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ProductSearchService searchService;
    private final InventoryService inventoryService;

    @Autowired
    public SearchController(@Autowired(required = false) ProductSearchService searchService,
                           InventoryService inventoryService) {
        this.searchService = searchService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<?>>> search(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        // If Elasticsearch is available, use it
        if (searchService != null) {
            List<ProductDocument> results = searchService.searchWithFilters(q, category, minPrice, maxPrice);
            return ResponseEntity.ok(ApiResponse.success(results));
        }
        
        // Fallback to simple database search
        List<Product> allProducts = inventoryService.getAllProducts();
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(q.toLowerCase()) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(q.toLowerCase())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(filtered));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(@RequestParam String q) {
        // If Elasticsearch is available, use it
        if (searchService != null) {
            List<String> suggestions = searchService.getSuggestions(q);
            return ResponseEntity.ok(ApiResponse.success(suggestions));
        }
        
        // Fallback to simple database search
        List<Product> products = inventoryService.getAllProducts();
        List<String> suggestions = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(q.toLowerCase()))
                .map(Product::getName)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
