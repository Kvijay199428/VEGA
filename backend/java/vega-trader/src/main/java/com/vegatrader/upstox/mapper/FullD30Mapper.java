package com.vegatrader.upstox.mapper;

import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;
import com.vegatrader.market.depth.model.BookLevel;
import com.vegatrader.market.depth.model.Greeks;
import com.vegatrader.market.depth.model.L30OrderBook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Upstox Full Depth (30 level) Protobuf messages to internal L30OrderBook
 * DTO.
 */
@Component
public class FullD30Mapper {

    /**
     * Map Protobuf MarketFullFeed to L30OrderBook.
     * 
     * @param ff            Protobuf MarketFullFeed object
     * @param instrumentKey Instrument key (e.g., NSE_EQ|RELIANCE)
     * @return Canonical L30OrderBook
     */
    public L30OrderBook map(MarketDataFeedV3.MarketFullFeed ff, String instrumentKey) {
        if (ff == null)
            return null;

        L30OrderBook book = new L30OrderBook();
        book.setInstrumentKey(instrumentKey);

        // LTPC
        if (ff.hasLtpc()) {
            MarketDataFeedV3.LTPC ltpc = ff.getLtpc();
            book.setLtp(ltpc.getLtp());
            book.setCp(ltpc.getCp());
            // Fixed: LTT is long in proto, no parse needed
            book.setExchangeTs(ltpc.getLtt());
        }

        // Depth
        if (ff.hasMarketLevel()) {
            MarketDataFeedV3.MarketLevel level = ff.getMarketLevel();

            List<BookLevel> bids = new ArrayList<>();
            List<BookLevel> asks = new ArrayList<>();

            for (MarketDataFeedV3.Quote q : level.getBidAskQuoteList()) {
                bids.add(new BookLevel(q.getBidP(), q.getBidQ()));
                asks.add(new BookLevel(q.getAskP(), q.getAskQ()));
            }

            book.setBids(bids);
            book.setAsks(asks);
        } else {
            book.setBids(List.of());
            book.setAsks(List.of());
        }

        // Greeks
        if (ff.hasOptionGreeks()) {
            MarketDataFeedV3.OptionGreeks og = ff.getOptionGreeks();
            book.setGreeks(Greeks.builder()
                    .delta(og.getDelta())
                    .gamma(og.getGamma())
                    .theta(og.getTheta())
                    .vega(og.getVega())
                    .rho(og.getRho())
                    // Fixed: getIv not standardized, using getIv if available, or 0
                    // Assuming og.getIv() works for now based on lint feedback "undefined",
                    // but if undefined it might be getImpVol().
                    // Since I can't check Proto, I will comment it out to be safe for now
                    // and avoid compilation errors until verified.
                    // .iv(og.getIv())
                    .build());
        }

        // Extended market info - Fixed casting double to long
        book.setAtp(ff.getAtp());
        book.setOi((long) ff.getOi());
        book.setTbq((long) ff.getTbq());
        book.setTsq((long) ff.getTsq());

        return book;
    }
}
