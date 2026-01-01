package com.vegatrader.market.depth;

import com.vegatrader.market.depth.model.L30OrderBook;
import com.vegatrader.market.depth.model.BookLevel;
import com.vegatrader.market.dto.DepthLevel;
import com.vegatrader.market.dto.OrderBookSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Compresses full 30-level order book to smaller representations.
 * Used to reduce bandwidth for frontend WebSocket delivery.
 */
@Component
public class OrderBookCompressor {

    /** Top 5 levels (mobile) */
    public static final int LEVELS_5 = 5;

    /** Top 10 levels (standard UI) */
    public static final int LEVELS_10 = 10;

    /** Full 30 levels (pro UI) */
    public static final int LEVELS_30 = 30;

    /**
     * Compress L30OrderBook to OrderBookSnapshot with specified depth.
     * 
     * @param book   Full 30-level book
     * @param levels Number of levels to keep (5, 10, or 30)
     * @return Compressed snapshot
     */
    public OrderBookSnapshot compress(L30OrderBook book, int levels) {
        if (book == null)
            return null;

        List<DepthLevel> bids = book.getBids() == null ? List.of()
                : book.getBids().stream()
                        .limit(levels)
                        .map(this::toDepthLevel)
                        .collect(Collectors.toList());

        List<DepthLevel> asks = book.getAsks() == null ? List.of()
                : book.getAsks().stream()
                        .limit(levels)
                        .map(this::toDepthLevel)
                        .collect(Collectors.toList());

        return OrderBookSnapshot.builder()
                .instrumentKey(book.getInstrumentKey())
                .bids(bids)
                .asks(asks)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Compress to default 5 levels.
     */
    public OrderBookSnapshot compressToTop5(L30OrderBook book) {
        return compress(book, LEVELS_5);
    }

    /**
     * Compress to 10 levels.
     */
    public OrderBookSnapshot compressToTop10(L30OrderBook book) {
        return compress(book, LEVELS_10);
    }

    private DepthLevel toDepthLevel(BookLevel bl) {
        return new DepthLevel(bl.getPrice(), bl.getQuantity(), bl.getOrders());
    }
}
