package com.orderly.inventory.controller;

import com.orderly.common.dto.ApiResponse;
import com.orderly.inventory.search.ProductDocument;
import com.orderly.inventory.search.ProductSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ProductSearchService searchService;

    public SearchController(ProductSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDocument>>> search(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<ProductDocument> results = searchService.searchWithFilters(q, category, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSuggestions(@RequestParam String q) {
        List<String> suggestions = searchService.getSuggestions(q);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
