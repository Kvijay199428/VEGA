package com.vegatrader.execution.dto;

import com.vegatrader.domain.enums.OrderType;
import com.vegatrader.domain.enums.ProductType;
import com.vegatrader.domain.enums.TransactionType;
import com.vegatrader.domain.enums.ValidityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String instrumentKey;
    private int quantity;
    private double price;
    private OrderType orderType; // MARKET, LIMIT...
    private TransactionType transactionType; // BUY, SELL
    private ProductType productType; // INTRA, CNC...
    private ValidityType validity; // DAY, IOC
    private String tag;
}
