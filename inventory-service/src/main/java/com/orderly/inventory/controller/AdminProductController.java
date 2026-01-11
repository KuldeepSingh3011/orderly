package com.orderly.inventory.controller;

import com.orderly.common.dto.ApiResponse;
import com.orderly.inventory.entity.Product;
import com.orderly.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin endpoints for product management.
 * These endpoints require ADMIN role (enforced by API Gateway/Auth).
 */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final InventoryService inventoryService;

    public AdminProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        List<Product> products = includeInactive 
                ? inventoryService.getAllProductsIncludingInactive()
                : inventoryService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product created = inventoryService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable String productId,
            @RequestBody Product productUpdate) {
        try {
            Product updated = inventoryService.updateProduct(productId, productUpdate);
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{productId}/price")
    public ResponseEntity<ApiResponse<Product>> updatePrice(
            @PathVariable String productId,
            @RequestParam BigDecimal price) {
        try {
            Product updated = inventoryService.updatePrice(productId, price);
            return ResponseEntity.ok(ApiResponse.success("Price updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<Product>> updateStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            Product updated = inventoryService.updateStock(productId, quantity);
            return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{productId}/stock/adjust")
    public ResponseEntity<ApiResponse<Product>> adjustStock(
            @PathVariable String productId,
            @RequestParam int adjustment) {
        try {
            Product updated = inventoryService.adjustStock(productId, adjustment);
            return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productId) {
        try {
            inventoryService.deleteProduct(productId);
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{productId}/activate")
    public ResponseEntity<ApiResponse<Product>> activateProduct(@PathVariable String productId) {
        try {
            Product updated = inventoryService.setProductActive(productId, true);
            return ResponseEntity.ok(ApiResponse.success("Product activated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{productId}/deactivate")
    public ResponseEntity<ApiResponse<Product>> deactivateProduct(@PathVariable String productId) {
        try {
            Product updated = inventoryService.setProductActive(productId, false);
            return ResponseEntity.ok(ApiResponse.success("Product deactivated", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
