package com.vegatrader.upstox.auth.selenium.config;

/**
 * DTO for storing login credentials.
 *
 * @since 2.0.0
 */
public class LoginCredentials {

    private String username;
    private String password;
    private String totpSecret; // For TOTP/2FA if needed

    public LoginCredentials() {
    }

    public LoginCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginCredentials(String username, String password, String totpSecret) {
        this.username = username;
        this.password = password;
        this.totpSecret = totpSecret;
    }

    /**
     * Validate credentials.
     */
    public void validate() {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    // Getters/Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public boolean hasTotpSecret() {
        return totpSecret != null && !totpSecret.isEmpty();
    }

    /**
     * Generate TOTP code using the secret.
     * 
     * @return 6-digit TOTP code or null if generation fails
     */
    public String generateTOTPCode() {
        if (!hasTotpSecret()) {
            return null;
        }

        // Support for manual OTP entry (direct 6-digit code)
        if (totpSecret.matches("^\\d{6}$")) {
            return totpSecret;
        }

        try {
            // TOTP parameters
            long time = System.currentTimeMillis() / 1000L; // Current time in seconds
            long timeStep = 30; // 30-second time step (standard)
            long timeIndex = time / timeStep;

            // Decode base32 secret
            byte[] keyBytes = decodeBase32(totpSecret);

            // Generate HMAC-SHA1
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (timeIndex & 0xff);
                timeIndex >>= 8;
            }

            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(keyBytes, "RAW");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data);

            // Get offset and generate code
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

            int otp = binary % 1000000;
            return String.format("%06d", otp);

        } catch (Exception e) {
            System.err.println("Error generating TOTP: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decode base32 string to bytes.
     */
    private byte[] decodeBase32(String base32) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        base32 = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");

        int outputLength = base32.length() * 5 / 8;
        byte[] output = new byte[outputLength];

        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (char c : base32.toCharArray()) {
            int val = alphabet.indexOf(c);
            if (val < 0)
                continue;

            buffer <<= 5;
            buffer |= val;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                output[count++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }

        return output;
    }

    @Override
    public String toString() {
        return String.format("LoginCredentials{username='%s', hasTOTP=%b}",
                username, hasTotpSecret());
    }
}
