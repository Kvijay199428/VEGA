package com.vegatrader.analytics.valuation;

/**
 * Standard Black-Scholes-Merton option pricing model.
 */
public final class BlackScholesPricer {

    private BlackScholesPricer() {
    }

    public static double call(double S, double K, double T, double r, double sigma) {
        if (T <= 0)
            return Math.max(0, S - K);
        sigma = Math.max(sigma, 1e-6);

        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T)
                / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);

        return S * normalCdf(d1) - K * Math.exp(-r * T) * normalCdf(d2);
    }

    public static double put(double S, double K, double T, double r, double sigma) {
        if (T <= 0)
            return Math.max(0, K - S);
        sigma = Math.max(sigma, 1e-6);

        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T)
                / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);

        return K * Math.exp(-r * T) * normalCdf(-d2) - S * normalCdf(-d1);
    }

    private static double normalCdf(double x) {
        return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
    }

    private static double erf(double z) {
        // Abramowitz & Stegun approximation
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));
        double ans = 1 - t * Math.exp(
                -z * z - 1.26551223 +
                        t * (1.00002368 +
                                t * (0.37409196 +
                                        t * (0.09678418 +
                                                t * (-0.18628806 +
                                                        t * (0.27886807 +
                                                                t * (-1.13520398 +
                                                                        t * (1.48851587 +
                                                                                t * (-0.82215223 +
                                                                                        t * 0.17087277)))))))));
        return z >= 0 ? ans : -ans;
    }
    // === Pricing Aliases (for API consistency) ===

    public static double calculateCallPrice(double S, double K, double T, double r, double sigma) {
        return call(S, K, T, r, sigma);
    }

    public static double calculatePutPrice(double S, double K, double T, double r, double sigma) {
        return put(S, K, T, r, sigma);
    }

    // === Greeks ===

    public static double calculateCallDelta(double S, double K, double T, double r, double sigma) {
        if (T <= 0)
            return 0.0;
        sigma = Math.max(sigma, 1e-6);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        return normalCdf(d1);
    }

    public static double calculatePutDelta(double S, double K, double T, double r, double sigma) {
        return calculateCallDelta(S, K, T, r, sigma) - 1.0;
    }

    public static double calculateGamma(double S, double K, double T, double r, double sigma) {
        if (T <= 0)
            return 0.0;
        sigma = Math.max(sigma, 1e-6);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double pdf = Math.exp(-0.5 * d1 * d1) / Math.sqrt(2 * Math.PI);
        return pdf / (S * sigma * Math.sqrt(T));
    }

    public static double calculateVega(double S, double K, double T, double r, double sigma) {
        if (T <= 0)
            return 0.0;
        sigma = Math.max(sigma, 1e-6);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double pdf = Math.exp(-0.5 * d1 * d1) / Math.sqrt(2 * Math.PI);
        return S * Math.sqrt(T) * pdf / 100.0; // Usually divided by 100 for percentage change
    }

    public static double calculateTheta(double S, double K, double T, double r, double sigma, boolean isCall) {
        if (T <= 0)
            return 0.0;
        sigma = Math.max(sigma, 1e-6);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);
        double pdf = Math.exp(-0.5 * d1 * d1) / Math.sqrt(2 * Math.PI);

        double term1 = -(S * pdf * sigma) / (2 * Math.sqrt(T));

        if (isCall) {
            double term2 = -r * K * Math.exp(-r * T) * normalCdf(d2);
            return (term1 + term2) / 365.0; // Daily theta
        } else {
            double term2 = r * K * Math.exp(-r * T) * normalCdf(-d2);
            return (term1 + term2) / 365.0; // Daily theta
        }
    }

    public static double calculateRho(double S, double K, double T, double r, double sigma, boolean isCall) {
        if (T <= 0)
            return 0.0;
        sigma = Math.max(sigma, 1e-6);
        double d1 = (Math.log(S / K) + (r + 0.5 * sigma * sigma) * T) / (sigma * Math.sqrt(T));
        double d2 = d1 - sigma * Math.sqrt(T);

        if (isCall) {
            return K * T * Math.exp(-r * T) * normalCdf(d2) / 100.0;
        } else {
            return -K * T * Math.exp(-r * T) * normalCdf(-d2) / 100.0;
        }
    }
}
