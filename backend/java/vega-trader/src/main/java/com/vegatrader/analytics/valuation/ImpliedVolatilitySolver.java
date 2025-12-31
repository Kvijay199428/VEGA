package com.vegatrader.analytics.valuation;

/**
 * Newton-Raphson solver for Implied Volatility.
 */
public final class ImpliedVolatilitySolver {

    private static final int MAX_ITER = 100;
    private static final double TOL = 1e-6;

    private ImpliedVolatilitySolver() {
    }

    public static double calculateIV(
            double marketPrice,
            double S,
            double K,
            double T,
            double r,
            boolean isCall) {
        return calculateIV(marketPrice, S, K, T, r, isCall, 0.5, 2.0, 0.0);
    }

    public static double calculateIV(
            double marketPrice,
            double S,
            double K,
            double T,
            double r,
            boolean isCall,
            double initialGuess,
            double maxIV,
            double fallbackIV) {
        if (T <= 0 || marketPrice <= 0)
            return 0.0;

        double sigma = initialGuess;

        for (int i = 0; i < MAX_ITER; i++) {
            double price = isCall
                    ? BlackScholesPricer.calculateCallPrice(S, K, T, r, sigma)
                    : BlackScholesPricer.calculatePutPrice(S, K, T, r, sigma);

            double diff = price - marketPrice;
            if (Math.abs(diff) < TOL)
                break;

            double vega = BlackScholesPricer.calculateVega(S, K, T, r, sigma) * 100.0; 
            // Note: BlackScholesPricer.calculateVega divides by 100. Here we need raw sensitivity?
            // Newton-Raphson: sigma_new = sigma - (Price - Market) / Vega
            // Price is ~10.45. Vega ~ 37 (if S=100).
            // If BS.calculateVega returns S*sqrt(T)*pdf/100, then it is 0.37.
            // (Price - Market) ~ 1.0 (e.g.). 1.0 / 0.37 = 2.7 -> Sigma change 270%?
            // Standard Vega is usually defined as dV/dSigma.
            // dV/dSigma = S * sqrt(T) * pdf.
            // BS.calculateVega divides by 100 to give change per 1% vol.
            // Newton method uses dV/dSigma (raw derivative).
            // So we must multiply BS.calculateVega by 100.

            if (vega < 1e-6)
                break;

            sigma -= diff / vega;

            if (sigma <= 0 || sigma > maxIV) {
                return fallbackIV;
            }
        }
        return sigma;
    }
}
