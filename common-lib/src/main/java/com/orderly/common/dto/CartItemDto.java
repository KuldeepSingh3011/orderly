package com.orderly.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * Represents an item in the shopping cart.
 */
public class CartItemDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private BigDecimal price;

    public CartItemDto() {
    }

    public CartItemDto(String productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Calculate total price for this cart item.
     */
    public BigDecimal getTotalPrice() {
        if (price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
