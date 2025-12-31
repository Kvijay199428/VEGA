package com.vegatrader.upstox.api.order.repository;

import com.vegatrader.upstox.api.order.entity.TradeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Trade entity.
 * Per order-mgmt/b1.md and b2.md.
 * 
 * @since 4.9.0
 */
@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Long> {

    /**
     * Find by trade ID.
     */
    Optional<TradeEntity> findByTradeId(String tradeId);

    /**
     * Find trades for an order.
     */
    List<TradeEntity> findByOrderId(String orderId);

    /**
     * Find trades for a user.
     */
    List<TradeEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find trades for user with pagination.
     */
    Page<TradeEntity> findByUserId(String userId, Pageable pageable);

    /**
     * Find trades for today.
     */
    List<TradeEntity> findByUserIdAndTradeDate(String userId, LocalDate tradeDate);

    /**
     * Find trades by segment.
     */
    List<TradeEntity> findByUserIdAndSegment(String userId, String segment);

    /**
     * Find trade history with date range and segment filter.
     */
    @Query("SELECT t FROM TradeEntity t WHERE t.userId = :userId " +
            "AND (:segment IS NULL OR t.segment = :segment) " +
            "AND t.tradeDate >= :startDate AND t.tradeDate <= :endDate " +
            "ORDER BY t.tradeDate DESC, t.createdAt DESC")
    Page<TradeEntity> findTradeHistory(
            @Param("userId") String userId,
            @Param("segment") String segment,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Count trades for order.
     */
    long countByOrderId(String orderId);

    /**
     * Get VWAP for order trades.
     */
    @Query("SELECT SUM(t.quantity * t.price) / SUM(t.quantity) FROM TradeEntity t WHERE t.orderId = :orderId")
    BigDecimal calculateVWAP(@Param("orderId") String orderId);

    /**
     * Get total filled quantity for order.
     */
    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM TradeEntity t WHERE t.orderId = :orderId")
    Integer getTotalFilledQuantity(@Param("orderId") String orderId);

    /**
     * Get total charges for order trades.
     */
    @Query("SELECT COALESCE(SUM(t.totalCharges), 0) FROM TradeEntity t WHERE t.orderId = :orderId")
    BigDecimal getTotalCharges(@Param("orderId") String orderId);

    /**
     * Get order trade summary (for b1.md section 4.4).
     */
    @Query("SELECT COUNT(t), SUM(t.quantity), MIN(t.createdAt), MAX(t.createdAt) " +
            "FROM TradeEntity t WHERE t.orderId = :orderId")
    Object[] getOrderTradeSummary(@Param("orderId") String orderId);

    /**
     * Find trades by exchange order ID.
     */
    List<TradeEntity> findByExchangeOrderId(String exchangeOrderId);
}
