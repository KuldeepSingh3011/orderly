package com.orderly.inventory.service;

import com.orderly.inventory.entity.Product;
import com.orderly.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        return productRepository.save(product);
    }

    public Product updateStock(String productId, int newQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }
}
