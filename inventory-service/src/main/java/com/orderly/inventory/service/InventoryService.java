package com.orderly.inventory.service;

import com.orderly.inventory.entity.Product;
import com.orderly.inventory.repository.ProductRepository;
import com.orderly.inventory.search.ProductSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ProductRepository productRepository;
    private final ProductSearchService searchService;

    @Autowired
    public InventoryService(ProductRepository productRepository, 
                           @Autowired(required = false) ProductSearchService searchService) {
        this.productRepository = productRepository;
        this.searchService = searchService;
    }

    private void indexProduct(Product product) {
        if (searchService == null) {
            return; // Elasticsearch not configured
        }
        try {
            searchService.indexProduct(product);
        } catch (Exception e) {
            log.warn("Failed to index product in Elasticsearch: {}", e.getMessage());
        }
    }

    /**
     * Reserve stock for an order item.
     * Uses optimistic locking to handle concurrent updates.
     */
    public boolean reserveStock(String productId, int quantity) {
        int attempts = 0;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                Product product = productRepository.findById(productId).orElse(null);
                if (product == null) {
                    log.warn("Product not found: {}", productId);
                    return false;
                }

                if (!product.hasAvailableStock(quantity)) {
                    log.warn("Insufficient stock for product {}: requested={}, available={}",
                            productId, quantity, product.getAvailableQuantity());
                    return false;
                }

                product.setReservedQuantity(product.getReservedQuantity() + quantity);
                productRepository.save(product);

                log.info("Reserved {} units of product {}. Remaining available: {}",
                        quantity, productId, product.getAvailableQuantity());
                return true;

            } catch (OptimisticLockingFailureException e) {
                attempts++;
                log.warn("Optimistic lock conflict for product {}, attempt {}/{}",
                        productId, attempts, MAX_RETRY_ATTEMPTS);
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error("Failed to reserve stock after {} attempts", MAX_RETRY_ATTEMPTS);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Release reserved stock (e.g., when order is cancelled).
     */
    public void releaseStock(String productId, int quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            int newReserved = Math.max(0, product.getReservedQuantity() - quantity);
            product.setReservedQuantity(newReserved);
            productRepository.save(product);
            log.info("Released {} units of product {}", quantity, productId);
        }
    }

    /**
     * Confirm stock deduction (convert reserved to actual deduction).
     */
    public void confirmStockDeduction(String productId, int quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setStockQuantity(product.getStockQuantity() - quantity);
            product.setReservedQuantity(product.getReservedQuantity() - quantity);
            productRepository.save(product);
            log.info("Confirmed stock deduction of {} units for product {}", quantity, productId);
        }
    }

    public Optional<Product> getProduct(String productId) {
        return productRepository.findById(productId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product createProduct(Product product) {
        Product saved = productRepository.save(product);
        indexProduct(saved);
        return saved;
    }

    public Product updateStock(String productId, int newQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }

    public List<Product> getAllProductsIncludingInactive() {
        return productRepository.findAll();
    }

    public Product updateProduct(String productId, Product productUpdate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (productUpdate.getName() != null) {
            product.setName(productUpdate.getName());
        }
        if (productUpdate.getDescription() != null) {
            product.setDescription(productUpdate.getDescription());
        }
        if (productUpdate.getCategory() != null) {
            product.setCategory(productUpdate.getCategory());
        }
        if (productUpdate.getPrice() != null) {
            product.setPrice(productUpdate.getPrice());
        }
        if (productUpdate.getImageUrl() != null) {
            product.setImageUrl(productUpdate.getImageUrl());
        }
        if (productUpdate.getSku() != null) {
            product.setSku(productUpdate.getSku());
        }

        log.info("Updated product: {}", productId);
        Product saved = productRepository.save(product);
        indexProduct(saved);
        return saved;
    }

    public Product updatePrice(String productId, java.math.BigDecimal newPrice) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setPrice(newPrice);
        log.info("Updated price for product {}: {}", productId, newPrice);
        Product saved = productRepository.save(product);
        indexProduct(saved);
        return saved;
    }

    public Product adjustStock(String productId, int adjustment) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        int newStock = product.getStockQuantity() + adjustment;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        product.setStockQuantity(newStock);
        log.info("Adjusted stock for product {} by {}: new stock = {}", productId, adjustment, newStock);
        return productRepository.save(product);
    }

    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        // Soft delete - just deactivate
        product.setActive(false);
        productRepository.save(product);
        log.info("Deleted (deactivated) product: {}", productId);
    }

    public Product setProductActive(String productId, boolean active) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setActive(active);
        log.info("Set product {} active status to: {}", productId, active);
        return productRepository.save(product);
    }
}
