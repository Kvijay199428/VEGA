package com.vegatrader.upstox.api.websocket.protocol;

import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;
import com.vegatrader.upstox.api.response.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper for converting Upstox Protobuf messages to DTOs.
 * 
 * <p>
 * This class isolates the Protobuf dependency and provides a clean mapping
 * to the existing MarketDataFeedV3Response structure, allowing the rest
 * of the system to remain agnostic of the transport format.
 */
public class MarketDataProtoMapper {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataProtoMapper.class);

    /**
     * Maps a Protobuf FeedResponse to a MarketDataFeedV3Response DTO.
     * 
     * @param proto the Protobuf response
     * @return the mapped DTO
     */
    public static MarketDataFeedV3Response mapResponse(MarketDataFeedV3.FeedResponse proto) {
        MarketDataFeedV3Response dto = new MarketDataFeedV3Response();

        // Map type
        dto.setType(mapType(proto.getType()));

        // Map feeds
        Map<String, FeedData> feeds = new HashMap<>();
        proto.getFeedsMap().forEach((key, feed) -> {
            try {
                feeds.put(key, mapFeed(feed));
            } catch (Exception e) {
                logger.warn("Failed to map feed for key {}: {}", key, e.getMessage());
            }
        });
        dto.setFeeds(feeds);

        return dto;
    }

    private static String mapType(MarketDataFeedV3.Type type) {
        return switch (type) {
            case initial_feed -> "initial_feed";
            case live_feed -> "live_feed";
            default -> "unknown";
        };
    }

    private static FeedData mapFeed(MarketDataFeedV3.Feed feed) {
        FeedData dto = new FeedData();

        // Handle the feed union cases
        switch (feed.getFeedUnionCase()) {
            case LTPC -> {
                dto.setLtpc(mapLtpc(feed.getLtpc()));
            }
            case FULLFEED -> {
                MarketDataFeedV3.FullFeed fullFeed = feed.getFullFeed();
                if (fullFeed.hasMarketFF()) {
                    mapMarketFullFeed(fullFeed.getMarketFF(), dto);
                } else if (fullFeed.hasIndexFF()) {
                    mapIndexFullFeed(fullFeed.getIndexFF(), dto);
                }
            }
            case FIRSTLEVELWITHGREEKS -> {
                MarketDataFeedV3.FirstLevelWithGreeks flwg = feed.getFirstLevelWithGreeks();
                if (flwg.hasLtpc()) {
                    dto.setLtpc(mapLtpc(flwg.getLtpc()));
                }
                if (flwg.hasOptionGreeks()) {
                    dto.setOptionGreeks(mapGreeks(flwg.getOptionGreeks()));
                }
            }
            default -> {
                logger.debug("Unhandled feed union case: {}", feed.getFeedUnionCase());
            }
        }

        return dto;
    }

    private static void mapMarketFullFeed(MarketDataFeedV3.MarketFullFeed marketFF, FeedData dto) {
        if (marketFF.hasLtpc()) {
            dto.setLtpc(mapLtpc(marketFF.getLtpc()));
        }
        if (marketFF.hasMarketLevel()) {
            dto.setMarketLevel(mapLevel(marketFF.getMarketLevel()));
        }
        if (marketFF.hasOptionGreeks()) {
            dto.setOptionGreeks(mapGreeks(marketFF.getOptionGreeks()));
        }
        if (marketFF.hasMarketOHLC()) {
            dto.setMarketOhlc(mapMarketOhlc(marketFF.getMarketOHLC()));
        }
        // Additional MarketFullFeed fields can be mapped here
        // atp, vtt, oi, iv, tbq, tsq, cp, lc, uc, etc.
    }

    private static void mapIndexFullFeed(MarketDataFeedV3.IndexFullFeed indexFF, FeedData dto) {
        if (indexFF.hasLtpc()) {
            dto.setLtpc(mapLtpc(indexFF.getLtpc()));
        }
        // Index-specific mapping can be added here
    }

    private static LTPCData mapLtpc(MarketDataFeedV3.LTPC ltpc) {
        LTPCData dto = new LTPCData();
        dto.setLtp(ltpc.getLtp());
        dto.setLtt(String.valueOf(ltpc.getLtt()));
        dto.setLtq(String.valueOf(ltpc.getLtq()));
        dto.setCp(ltpc.getCp());
        return dto;
    }

    private static MarketOHLCData mapMarketOhlc(MarketDataFeedV3.MarketOHLC ohlc) {
        MarketOHLCData dto = new MarketOHLCData();
        // MarketOHLC contains a list of OHLC entries for different intervals
        if (ohlc.getOhlcCount() > 0) {
            // Use the first OHLC entry (typically the session/day OHLC)
            MarketDataFeedV3.OHLC firstOhlc = ohlc.getOhlc(0);
            dto.setOpen(firstOhlc.getOpen());
            dto.setHigh(firstOhlc.getHigh());
            dto.setLow(firstOhlc.getLow());
            dto.setClose(firstOhlc.getClose());
            dto.setVolume(firstOhlc.getVol());
        }
        return dto;
    }

    private static MarketLevelData mapLevel(MarketDataFeedV3.MarketLevel level) {
        MarketLevelData dto = new MarketLevelData();
        List<BidAskQuote> quotes = new ArrayList<>();

        for (int i = 0; i < level.getBidAskQuoteCount(); i++) {
            quotes.add(mapQuote(level.getBidAskQuote(i)));
        }

        dto.setBidAskQuotes(quotes);
        return dto;
    }

    private static BidAskQuote mapQuote(MarketDataFeedV3.Quote quote) {
        BidAskQuote dto = new BidAskQuote();
        dto.setBq(quote.getBidQ());
        dto.setBp(quote.getBidP());
        // Note: Quote doesn't have bno field
        dto.setAq(quote.getAskQ());
        dto.setAp(quote.getAskP());
        // Note: Quote doesn't have ano field
        return dto;
    }

    private static OptionGreeksData mapGreeks(MarketDataFeedV3.OptionGreeks greeks) {
        OptionGreeksData dto = new OptionGreeksData();
        dto.setDelta(greeks.getDelta());
        dto.setTheta(greeks.getTheta());
        dto.setGamma(greeks.getGamma());
        dto.setVega(greeks.getVega());
        // Note: OptionGreeksData may not have all fields; check availability
        return dto;
    }
}
