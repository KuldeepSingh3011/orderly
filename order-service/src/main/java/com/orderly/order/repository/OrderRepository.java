package com.orderly.order.repository;

import com.orderly.common.constants.OrderStatus;
import com.orderly.order.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByUserId(String userId);

    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(Instant start, Instant end);

    long countByStatus(OrderStatus status);
}
