
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeedV3;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MarketFeederClient {

    private static final String ACCESS_TOKEN_FILE = "access_token.txt";
    private static final String LOG_FILE = "ws.log";
    private static PrintWriter logger;

    public static void main(String[] args) {
        try {
            // Setup logger
            logger = new PrintWriter(new FileWriter(LOG_FILE, true), true); // Append mode, auto-flush
            log("Starting MarketFeederClient...");

            // Read access token
            String accessToken = readAccessToken();
            log("Access Token read successfully.");

            // Get authorized WebSocket URL
            String wsUrl = getWebsocketUrl(accessToken);
            log("Resolved WebSocket URL: " + wsUrl);

            // Connect using WebSocket
            WebSocketClient client = createWebSocketClient(URI.create(wsUrl));
            client.connect();

            // Keep main thread alive
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
            }, 0, 1, TimeUnit.SECONDS);

        } catch (Exception e) {
            log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readAccessToken() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(ACCESS_TOKEN_FILE));
        if (lines.isEmpty()) {
            throw new IOException("access_token.txt is empty");
        }
        return lines.get(0).trim();
    }

    private static void log(String message) {
        System.out.println(message);
        if (logger != null) {
            logger.println(java.time.LocalDateTime.now() + " - " + message);
        }
    }

    public static String getWebsocketUrl(String accessToken) throws IOException {
        String url = "https://api.upstox.com/v3/feed/market-data-feed/authorize";

        // Re-doing with manual redirect handling logic for clarity
        OkHttpClient noRedirectClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request manualRequest = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = noRedirectClient.newCall(manualRequest).execute()) {
            if (response.code() == 302) {
                String location = response.header("Location");
                if (location != null) {
                    return location;
                } else {
                    throw new IOException("Received 302 but no Location header");
                }
            } else if (response.isSuccessful()) {
                String body = response.body().string();
                log("Received 200 OK response, parsing JSON...");
                JsonObject jsonResponse = new Gson().fromJson(body, JsonObject.class);
                if (jsonResponse.has("data") && jsonResponse.get("data").isJsonObject()) {
                    JsonObject data = jsonResponse.getAsJsonObject("data");
                    if (data.has("authorizedRedirectUri")) {
                        return data.get("authorizedRedirectUri").getAsString();
                    }
                }
                throw new IOException("Could not find authorizedRedirectUri in response: " + body);
            } else {
                String body = response.body() != null ? response.body().string() : "null";
                throw new IOException("Failed to authorize: Code " + response.code() + ", Body: " + body);
            }
        }
    }

    private static WebSocketClient createWebSocketClient(URI serverUri) {
        return new WebSocketClient(serverUri) {

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log("Opened connection to " + serverUri);
                sendSubscriptionRequest(this);
            }

            @Override
            public void onMessage(String message) {
                log("Received Text Message: " + message);
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                handleBinaryMessage(bytes);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log("Connection closed by " + (remote ? "remote peer" : "us") + ". Code: " + code + ", Reason: "
                        + reason);
            }

            @Override
            public void onError(Exception ex) {
                log("WebSocket Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        };
    }

    private static void sendSubscriptionRequest(WebSocketClient client) {
        JsonObject requestObject = constructSubscriptionRequest();
        byte[] binaryData = requestObject.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        log("Sending Subscription Request: " + requestObject);
        client.send(binaryData);
    }

    private static JsonObject constructSubscriptionRequest() {
        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("mode", "full_d30"); // User requested full_d30

        JsonArray instrumentKeys = new Gson().toJsonTree(Arrays.asList("NSE_INDEX|Nifty Bank", "NSE_INDEX|Nifty 50"))
                .getAsJsonArray();
        dataObject.add("instrumentKeys", instrumentKeys);

        JsonObject mainObject = new JsonObject();
        mainObject.addProperty("guid", java.util.UUID.randomUUID().toString());
        mainObject.addProperty("method", "sub");
        mainObject.add("data", dataObject);

        return mainObject;
    }

    private static void handleBinaryMessage(ByteBuffer bytes) {
        try {
            // Note: usage of parseFrom(ByteBuffer)
            MarketDataFeedV3.FeedResponse feedResponse = MarketDataFeedV3.FeedResponse.parseFrom(bytes);

            log("Received Binary Message (FeedResponse):");
            log("Type: " + feedResponse.getType());

            if (feedResponse.hasMarketInfo()) {
                log("Market Info: " + feedResponse.getMarketInfo());
            }
            if (feedResponse.getFeedsCount() > 0) {
                log("Feeds Count: " + feedResponse.getFeedsCount());
                feedResponse.getFeedsMap().forEach((key, feed) -> {
                    log("Key: " + key);
                    if (feed.hasLtpc()) {
                        log("  LTPC: LTP=" + feed.getLtpc().getLtp() + ", LTT=" + feed.getLtpc().getLtt());
                    }
                    if (feed.hasFullFeed()) {
                        MarketDataFeedV3.FullFeed fullFeed = feed.getFullFeed();
                        if (fullFeed.hasMarketFF()) {
                            MarketDataFeedV3.MarketFullFeed marketFF = fullFeed.getMarketFF();
                            if (marketFF.hasLtpc()) {
                                log("  FullFeed Market LTP: " + marketFF.getLtpc().getLtp());
                            }
                        } else if (fullFeed.hasIndexFF()) {
                            MarketDataFeedV3.IndexFullFeed indexFF = fullFeed.getIndexFF();
                            if (indexFF.hasLtpc()) {
                                log("  FullFeed Index LTP: " + indexFF.getLtpc().getLtp());
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            log("Failed to parse binary message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}