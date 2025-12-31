package com.vegatrader.upstox.auth.selenium.v2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies access token by calling Upstox Profile API.
 * Returns user profile data including email, exchanges, and account status.
 *
 * @since 2.1.0
 */
public class ProfileVerifierV2 {

    private static final Logger logger = LoggerFactory.getLogger(ProfileVerifierV2.class);

    private static final String PROFILE_URL = "https://api.upstox.com/v2/user/profile";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final Gson gson;

    public ProfileVerifierV2() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.gson = new Gson();
    }

    /**
     * Verify token by calling Profile API.
     * 
     * @param accessToken access token to verify
     * @return true if token is valid (200 OK response)
     */
    public boolean verifyToken(String accessToken) {
        try {
            ProfileDataV2 profile = getProfile(accessToken);
            return profile != null && profile.isActive();
        } catch (Exception e) {
            logger.error("Token verification failed", e);
            return false;
        }
    }

    /**
     * Get user profile data.
     * 
     * @param accessToken valid access token
     * @return ProfileDataV2 with user details
     * @throws RuntimeException if API call fails
     */
    public ProfileDataV2 getProfile(String accessToken) {
        logger.info("Fetching user profile from Upstox API");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PROFILE_URL))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .timeout(TIMEOUT)
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String body = response.body();

            logger.debug("Profile API response: {} - {}", statusCode, body);

            if (statusCode == 401) {
                logger.warn("Token is invalid or expired (401 Unauthorized)");
                throw new RuntimeException("Token is invalid or expired");
            }

            if (statusCode != 200) {
                logger.error("Profile API failed: {} - {}", statusCode, body);
                throw new RuntimeException("Profile API failed with status " + statusCode);
            }

            ProfileDataV2 profile = parseProfileResponse(body);
            logger.info("✓ Profile verified: userId={}, userName={}",
                    profile.getUserId(), profile.getUserName());

            return profile;

        } catch (IOException | InterruptedException e) {
            logger.error("Profile API request failed", e);
            throw new RuntimeException("Profile API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse profile response JSON.
     * 
     * @param json response body
     * @return ProfileDataV2
     */
    private ProfileDataV2 parseProfileResponse(String json) {
        try {
            JsonObject root = gson.fromJson(json, JsonObject.class);

            if (!root.has("data")) {
                throw new RuntimeException("Invalid profile response: missing 'data' field");
            }

            JsonObject data = root.getAsJsonObject("data");
            ProfileDataV2 profile = new ProfileDataV2();

            if (data.has("email")) {
                profile.setEmail(data.get("email").getAsString());
            }
            if (data.has("user_id")) {
                profile.setUserId(data.get("user_id").getAsString());
            }
            if (data.has("user_name")) {
                profile.setUserName(data.get("user_name").getAsString());
            }
            if (data.has("broker")) {
                profile.setBroker(data.get("broker").getAsString());
            }
            if (data.has("user_type")) {
                profile.setUserType(data.get("user_type").getAsString());
            }
            if (data.has("poa")) {
                profile.setPoa(data.get("poa").getAsBoolean());
            }
            if (data.has("ddpi")) {
                profile.setDdpi(data.get("ddpi").getAsBoolean());
            }
            if (data.has("is_active")) {
                profile.setActive(data.get("is_active").getAsBoolean());
            }

            // Parse exchanges array
            if (data.has("exchanges") && data.get("exchanges").isJsonArray()) {
                List<String> exchanges = new ArrayList<>();
                for (var elem : data.getAsJsonArray("exchanges")) {
                    exchanges.add(elem.getAsString());
                }
                profile.setExchanges(exchanges);
            }

            // Parse products array
            if (data.has("products") && data.get("products").isJsonArray()) {
                List<String> products = new ArrayList<>();
                for (var elem : data.getAsJsonArray("products")) {
                    products.add(elem.getAsString());
                }
                profile.setProducts(products);
            }

            // Parse order_types array
            if (data.has("order_types") && data.get("order_types").isJsonArray()) {
                List<String> orderTypes = new ArrayList<>();
                for (var elem : data.getAsJsonArray("order_types")) {
                    orderTypes.add(elem.getAsString());
                }
                profile.setOrderTypes(orderTypes);
            }

            return profile;

        } catch (Exception e) {
            logger.error("Failed to parse profile response", e);
            throw new RuntimeException("Failed to parse profile response: " + e.getMessage(), e);
        }
    }

    /**
     * Profile data DTO.
     */
    public static class ProfileDataV2 {
        private String email;
        private String userId;
        private String userName;
        private String broker;
        private String userType;
        private boolean poa;
        private boolean ddpi;
        private boolean active;
        private List<String> exchanges;
        private List<String> products;
        private List<String> orderTypes;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getBroker() {
            return broker;
        }

        public void setBroker(String broker) {
            this.broker = broker;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public boolean isPoa() {
            return poa;
        }

        public void setPoa(boolean poa) {
            this.poa = poa;
        }

        public boolean isDdpi() {
            return ddpi;
        }

        public void setDdpi(boolean ddpi) {
            this.ddpi = ddpi;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public List<String> getExchanges() {
            return exchanges;
        }

        public void setExchanges(List<String> exchanges) {
            this.exchanges = exchanges;
        }

        public List<String> getProducts() {
            return products;
        }

        public void setProducts(List<String> products) {
            this.products = products;
        }

        public List<String> getOrderTypes() {
            return orderTypes;
        }

        public void setOrderTypes(List<String> orderTypes) {
            this.orderTypes = orderTypes;
        }

        @Override
        public String toString() {
            return "ProfileDataV2{" +
                    "userId='" + userId + '\'' +
                    ", userName='" + userName + '\'' +
                    ", broker='" + broker + '\'' +
                    ", active=" + active +
                    ", exchanges=" + exchanges +
                    '}';
        }
    }
}
