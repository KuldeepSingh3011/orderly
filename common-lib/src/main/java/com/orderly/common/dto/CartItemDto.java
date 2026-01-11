package com.orderly.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an item in the shopping cart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    private BigDecimal price;

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
