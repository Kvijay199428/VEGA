package com.vegatrader.upstox.api.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Trade JPA entity for persistence.
 * Per order-mgmt/b1.md and b2.md.
 * 
 * @since 4.9.0
 */
@Entity
@Table(name = "trades", indexes = {
        @Index(name = "idx_trades_order_id", columnList = "order_id"),
        @Index(name = "idx_trades_user_id", columnList = "user_id"),
        @Index(name = "idx_trades_segment", columnList = "segment"),
        @Index(name = "idx_trades_trade_date", columnList = "trade_date")
})
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_id", nullable = false, unique = true, length = 50)
    private String tradeId;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "exchange_order_id", length = 50)
    private String exchangeOrderId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    // Instrument
    @Column(name = "exchange", length = 10)
    private String exchange;

    @Column(name = "segment", length = 10)
    private String segment; // EQ, FO, COM, CD, MF

    @Column(name = "trading_symbol", length = 100)
    private String tradingSymbol;

    @Column(name = "instrument_token", length = 100)
    private String instrumentToken;

    @Column(name = "isin", length = 20)
    private String isin;

    // Trade details
    @Column(name = "transaction_type", length = 10)
    private String transactionType; // BUY, SELL

    @Column(name = "product", length = 10)
    private String product; // D, I, CO, MTF

    @Column(name = "order_type", length = 10)
    private String orderType; // MARKET, LIMIT, SL, SL-M

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "average_price", precision = 15, scale = 4)
    private BigDecimal averagePrice;

    @Column(name = "amount", precision = 15, scale = 4)
    private BigDecimal amount;

    // Charges (per b2.md section 5)
    @Column(name = "brokerage", precision = 15, scale = 4)
    private BigDecimal brokerage;

    @Column(name = "stt", precision = 15, scale = 4)
    private BigDecimal stt;

    @Column(name = "exchange_fees", precision = 15, scale = 4)
    private BigDecimal exchangeFees;

    @Column(name = "gst", precision = 15, scale = 4)
    private BigDecimal gst;

    @Column(name = "sebi_charges", precision = 15, scale = 4)
    private BigDecimal sebiCharges;

    @Column(name = "stamp_duty", precision = 15, scale = 4)
    private BigDecimal stampDuty;

    @Column(name = "total_charges", precision = 15, scale = 4)
    private BigDecimal totalCharges;

    // Value
    @Column(name = "gross_value", precision = 15, scale = 4)
    private BigDecimal grossValue;

    @Column(name = "net_value", precision = 15, scale = 4)
    private BigDecimal netValue;

    // FO fields
    @Column(name = "option_type", length = 5)
    private String optionType; // CE, PE

    @Column(name = "strike_price", precision = 15, scale = 4)
    private BigDecimal strikePrice;

    @Column(name = "expiry", length = 20)
    private String expiry;

    // Timestamps
    @Column(name = "exchange_timestamp")
    private Instant exchangeTimestamp;

    @Column(name = "order_timestamp")
    private Instant orderTimestamp;

    @Column(name = "trade_date")
    private java.time.LocalDate tradeDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Reference
    @Column(name = "order_ref_id", length = 50)
    private String orderRefId;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (tradeDate == null)
            tradeDate = java.time.LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getExchangeOrderId() {
        return exchangeOrderId;
    }

    public void setExchangeOrderId(String exchangeOrderId) {
        this.exchangeOrderId = exchangeOrderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public String getInstrumentToken() {
        return instrumentToken;
    }

    public void setInstrumentToken(String instrumentToken) {
        this.instrumentToken = instrumentToken;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
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

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBrokerage() {
        return brokerage;
    }

    public void setBrokerage(BigDecimal brokerage) {
        this.brokerage = brokerage;
    }

    public BigDecimal getStt() {
        return stt;
    }

    public void setStt(BigDecimal stt) {
        this.stt = stt;
    }

    public BigDecimal getExchangeFees() {
        return exchangeFees;
    }

    public void setExchangeFees(BigDecimal exchangeFees) {
        this.exchangeFees = exchangeFees;
    }

    public BigDecimal getGst() {
        return gst;
    }

    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public BigDecimal getSebiCharges() {
        return sebiCharges;
    }

    public void setSebiCharges(BigDecimal sebiCharges) {
        this.sebiCharges = sebiCharges;
    }

    public BigDecimal getStampDuty() {
        return stampDuty;
    }

    public void setStampDuty(BigDecimal stampDuty) {
        this.stampDuty = stampDuty;
    }

    public BigDecimal getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }

    public BigDecimal getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(BigDecimal grossValue) {
        this.grossValue = grossValue;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(BigDecimal strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public Instant getExchangeTimestamp() {
        return exchangeTimestamp;
    }

    public void setExchangeTimestamp(Instant exchangeTimestamp) {
        this.exchangeTimestamp = exchangeTimestamp;
    }

    public Instant getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(Instant orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public java.time.LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(java.time.LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getOrderRefId() {
        return orderRefId;
    }

    public void setOrderRefId(String orderRefId) {
        this.orderRefId = orderRefId;
    }
}
