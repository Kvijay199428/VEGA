package com.vegatrader.upstox.api.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order JPA entity for persistence.
 * Per order-mgmt/a1.md section 3.1.
 * 
 * @since 4.9.0
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_tag", columnList = "tag"),
        @Index(name = "idx_orders_placed_at", columnList = "placed_at")
})
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 50)
    private String orderId;

    @Column(name = "broker_order_id", length = 50)
    private String brokerOrderId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "broker", length = 20)
    private String broker = "UPSTOX";

    // Instrument
    @Column(name = "exchange", length = 10)
    private String exchange;

    @Column(name = "symbol", length = 50)
    private String symbol;

    @Column(name = "instrument_key", length = 100)
    private String instrumentKey;

    // Order details
    @Enumerated(EnumType.STRING)
    @Column(name = "side", length = 10)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 10)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "product", length = 10)
    private ProductType product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "trigger_price", precision = 15, scale = 4)
    private BigDecimal triggerPrice;

    @Column(name = "disclosed_quantity")
    private Integer disclosedQuantity = 0;

    @Column(name = "validity", length = 10)
    private String validity = "DAY";

    @Column(name = "tag", length = 50)
    private String tag;

    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "filled_quantity")
    private Integer filledQuantity = 0;

    @Column(name = "average_price", precision = 15, scale = 4)
    private BigDecimal averagePrice;

    @Column(name = "status_message", length = 500)
    private String statusMessage;

    // Timestamps
    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "final_status_at")
    private Instant finalStatusAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // RMS
    @Column(name = "rms_snapshot_id")
    private Long rmsSnapshotId;

    // AMO
    @Column(name = "is_amo")
    private Boolean isAmo = false;

    // Slice
    @Column(name = "is_sliced")
    private Boolean isSliced = false;

    @Column(name = "parent_order_id", length = 50)
    private String parentOrderId;

    // Enums
    public enum OrderSide {
        BUY, SELL
    }

    public enum OrderType {
        MARKET, LIMIT, SL, SL_M
    }

    public enum ProductType {
        I, D, CO, MTF
    }

    public enum OrderStatus {
        PENDING, ACKNOWLEDGED, OPEN, FILLED, PARTIALLY_FILLED, CANCELLED, REJECTED, EXPIRED
    }

    // Lifecycle
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (placedAt == null)
            placedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Helpers
    public boolean isComplete() {
        return status == OrderStatus.FILLED || status == OrderStatus.CANCELLED || status == OrderStatus.REJECTED;
    }

    public boolean isModifiable() {
        return status == OrderStatus.PENDING || status == OrderStatus.ACKNOWLEDGED ||
                status == OrderStatus.OPEN || status == OrderStatus.PARTIALLY_FILLED;
    }

    public int getPendingQuantity() {
        return quantity - (filledQuantity != null ? filledQuantity : 0);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getBrokerOrderId() {
        return brokerOrderId;
    }

    public void setBrokerOrderId(String brokerOrderId) {
        this.brokerOrderId = brokerOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public void setInstrumentKey(String instrumentKey) {
        this.instrumentKey = instrumentKey;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(OrderSide side) {
        this.side = side;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public ProductType getProduct() {
        return product;
    }

    public void setProduct(ProductType product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(BigDecimal triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public Integer getDisclosedQuantity() {
        return disclosedQuantity;
    }

    public void setDisclosedQuantity(Integer disclosedQuantity) {
        this.disclosedQuantity = disclosedQuantity;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Integer getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(Integer filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Instant placedAt) {
        this.placedAt = placedAt;
    }

    public Instant getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Instant acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public Instant getFinalStatusAt() {
        return finalStatusAt;
    }

    public void setFinalStatusAt(Instant finalStatusAt) {
        this.finalStatusAt = finalStatusAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getRmsSnapshotId() {
        return rmsSnapshotId;
    }

    public void setRmsSnapshotId(Long rmsSnapshotId) {
        this.rmsSnapshotId = rmsSnapshotId;
    }

    public Boolean getIsAmo() {
        return isAmo;
    }

    public void setIsAmo(Boolean isAmo) {
        this.isAmo = isAmo;
    }

    public Boolean getIsSliced() {
        return isSliced;
    }

    public void setIsSliced(Boolean isSliced) {
        this.isSliced = isSliced;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String parentOrderId) {
        this.parentOrderId = parentOrderId;
    }
}
