package com.vegatrader.upstox.api.order.repository;

import com.vegatrader.upstox.api.order.entity.OrderEntity;
import com.vegatrader.upstox.api.order.entity.OrderEntity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Order entity.
 * Per order-mgmt/a1.md and b1.md.
 * 
 * @since 4.9.0
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * Find by internal order ID.
     */
    Optional<OrderEntity> findByOrderId(String orderId);

    /**
     * Find by broker order ID.
     */
    Optional<OrderEntity> findByBrokerOrderId(String brokerOrderId);

    /**
     * Find by correlation ID (for multi-order tracking).
     */
    Optional<OrderEntity> findByCorrelationId(String correlationId);

    /**
     * Find all orders for a user.
     */
    List<OrderEntity> findByUserIdOrderByPlacedAtDesc(String userId);

    /**
     * Find orders by user with pagination.
     */
    Page<OrderEntity> findByUserId(String userId, Pageable pageable);

    /**
     * Find orders by user and status.
     */
    List<OrderEntity> findByUserIdAndStatus(String userId, OrderStatus status);

    /**
     * Find orders by user and tag (strategy).
     */
    List<OrderEntity> findByUserIdAndTag(String userId, String tag);

    /**
     * Find orders by status.
     */
    List<OrderEntity> findByStatus(OrderStatus status);

    /**
     * Find orders by tag.
     */
    List<OrderEntity> findByTag(String tag);

    /**
     * Find open/pending orders for user.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.status IN :statuses")
    List<OrderEntity> findActiveOrders(
            @Param("userId") String userId,
            @Param("statuses") List<OrderStatus> statuses);

    /**
     * Find orders placed on a specific date.
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.placedAt >= :start AND o.placedAt < :end ORDER BY o.placedAt DESC")
    List<OrderEntity> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Find sliced child orders.
     */
    List<OrderEntity> findByParentOrderId(String parentOrderId);

    /**
     * Count orders by user and status.
     */
    long countByUserIdAndStatus(String userId, OrderStatus status);

    /**
     * Get order book (today's orders).
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.placedAt >= :todayStart ORDER BY o.placedAt DESC")
    List<OrderEntity> getOrderBook(@Param("userId") String userId, @Param("todayStart") Instant todayStart);

    /**
     * Find by segment (exchange).
     */
    List<OrderEntity> findByUserIdAndExchange(String userId, String exchange);

    /**
     * Find modifiable orders (for cancel multi).
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.status IN ('PENDING', 'ACKNOWLEDGED', 'OPEN', 'PARTIALLY_FILLED')")
    List<OrderEntity> findModifiableOrders(@Param("userId") String userId);
}
