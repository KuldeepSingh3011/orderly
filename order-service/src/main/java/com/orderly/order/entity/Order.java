package com.orderly.order.entity;

import com.orderly.common.constants.OrderStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String userId;

    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;

    private OrderStatus status = OrderStatus.PENDING;

    private ShippingAddress shippingAddress;

    private String failureReason;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Order() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Order order = new Order();

        public Builder userId(String userId) {
            order.userId = userId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            order.items = items;
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            order.subtotal = subtotal;
            return this;
        }

        public Builder tax(BigDecimal tax) {
            order.tax = tax;
            return this;
        }

        public Builder shippingCost(BigDecimal shippingCost) {
            order.shippingCost = shippingCost;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            order.totalAmount = totalAmount;
            return this;
        }

        public Builder status(OrderStatus status) {
            order.status = status;
            return this;
        }

        public Builder shippingAddress(ShippingAddress shippingAddress) {
            order.shippingAddress = shippingAddress;
            return this;
        }

        public Order build() {
            return order;
        }
    }

    // Nested classes
    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal price;

        public OrderItem() {
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

        public BigDecimal getTotal() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final OrderItem item = new OrderItem();

            public Builder productId(String productId) {
                item.productId = productId;
                return this;
            }

            public Builder productName(String productName) {
                item.productName = productName;
                return this;
            }

            public Builder quantity(int quantity) {
                item.quantity = quantity;
                return this;
            }

            public Builder price(BigDecimal price) {
                item.price = price;
                return this;
            }

            public OrderItem build() {
                return item;
            }
        }
    }

    public static class ShippingAddress {
        private String fullName;
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
        private String phone;

        public ShippingAddress() {
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
