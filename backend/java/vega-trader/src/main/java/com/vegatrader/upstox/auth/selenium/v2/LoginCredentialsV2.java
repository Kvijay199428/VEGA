package com.vegatrader.upstox.auth.selenium.v2;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login credentials DTO with TOTP generation for V2 login automation.
 * Implements RFC 6238 TOTP algorithm for 2FA authentication.
 *
 * @since 2.1.0
 */
public class LoginCredentialsV2 {

    private static final Logger logger = LoggerFactory.getLogger(LoginCredentialsV2.class);

    private String mobileNumber; // 10-digit mobile number
    private String pin; // 6-digit PIN
    private String totpSecret; // Base32 encoded TOTP secret

    public LoginCredentialsV2() {
    }

    public LoginCredentialsV2(String mobileNumber, String pin, String totpSecret) {
        this.mobileNumber = mobileNumber;
        this.pin = pin;
        this.totpSecret = totpSecret;
    }

    /**
     * Validate credentials are complete.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required");
        }
        if (mobileNumber.length() != 10) {
            throw new IllegalArgumentException("Mobile number must be 10 digits");
        }
        if (pin == null || pin.isEmpty()) {
            throw new IllegalArgumentException("PIN is required");
        }
        if (pin.length() != 6) {
            throw new IllegalArgumentException("PIN must be 6 digits");
        }
    }

    /**
     * Check if TOTP secret is available.
     * 
     * @return true if TOTP secret is set
     */
    public boolean hasTotpSecret() {
        return totpSecret != null && !totpSecret.isEmpty();
    }

    /**
     * Generate TOTP code using RFC 6238 algorithm.
     * Uses HMAC-SHA1 with 30-second time step and 6-digit output.
     * 
     * @return 6-digit TOTP code, or null if generation fails
     */
    public String generateTotpCode() {
        if (!hasTotpSecret()) {
            logger.warn("No TOTP secret configured");
            return null;
        }

        try {
            // Current time in seconds
            long time = System.currentTimeMillis() / 1000L;
            long timeStep = 30; // Standard 30-second interval
            long timeIndex = time / timeStep;

            // Decode Base32 secret
            byte[] keyBytes = decodeBase32(totpSecret);
            if (keyBytes == null || keyBytes.length == 0) {
                logger.error("Failed to decode Base32 TOTP secret");
                return null;
            }

            // Convert time to 8-byte array (big-endian)
            byte[] data = new byte[8];
            long value = timeIndex;
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (value & 0xff);
                value >>= 8;
            }

            // Generate HMAC-SHA1
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "RAW");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data);

            // Dynamic truncation
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24) |
                    ((hash[offset + 1] & 0xff) << 16) |
                    ((hash[offset + 2] & 0xff) << 8) |
                    (hash[offset + 3] & 0xff);

            // Generate 6-digit code
            int otp = binary % 1000000;
            String code = String.format("%06d", otp);

            logger.debug("Generated TOTP code: {}", code);
            return code;

        } catch (Exception e) {
            logger.error("Failed to generate TOTP code", e);
            return null;
        }
    }

    /**
     * Decode Base32 string to byte array.
     * Supports standard Base32 alphabet (A-Z, 2-7).
     * 
     * @param base32 Base32 encoded string
     * @return decoded bytes
     */
    private byte[] decodeBase32(String base32) {
        if (base32 == null || base32.isEmpty()) {
            return new byte[0];
        }

        // Remove padding and convert to uppercase
        String cleanInput = base32.replaceAll("[= ]", "").toUpperCase();

        // Base32 alphabet
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        // Calculate output length
        int outputLength = (cleanInput.length() * 5) / 8;
        byte[] output = new byte[outputLength];

        int buffer = 0;
        int bitsLeft = 0;
        int outputIndex = 0;

        for (int i = 0; i < cleanInput.length(); i++) {
            char c = cleanInput.charAt(i);
            int value = alphabet.indexOf(c);

            if (value < 0) {
                logger.warn("Invalid Base32 character: {}", c);
                continue;
            }

            buffer = (buffer << 5) | value;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                output[outputIndex++] = (byte) ((buffer >> bitsLeft) & 0xff);
            }
        }

        return output;
    }

    // Getters and Setters

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    @Override
    public String toString() {
        return "LoginCredentialsV2{" +
                "mobileNumber='" + mobileNumber + '\'' +
                ", pin='****'" +
                ", hasTotpSecret=" + hasTotpSecret() +
                '}';
    }
}
