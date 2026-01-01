package com.vegatrader.market.depth.model;

public class Greeks {
    private double delta;
    private double gamma;
    private double theta;
    private double vega;
    private double rho;
    private double iv;

    public Greeks() {
    }

    // Getters
    public double getDelta() {
        return delta;
    }

    public double getGamma() {
        return gamma;
    }

    public double getTheta() {
        return theta;
    }

    public double getVega() {
        return vega;
    }

    public double getRho() {
        return rho;
    }

    public double getIv() {
        return iv;
    }

    // Setters
    public void setDelta(double delta) {
        this.delta = delta;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public void setVega(double vega) {
        this.vega = vega;
    }

    public void setRho(double rho) {
        this.rho = rho;
    }

    public void setIv(double iv) {
        this.iv = iv;
    }

    // Builder
    public static GreeksBuilder builder() {
        return new GreeksBuilder();
    }

    public static class GreeksBuilder {
        private Greeks greeks = new Greeks();

        public GreeksBuilder delta(double d) {
            greeks.setDelta(d);
            return this;
        }

        public GreeksBuilder gamma(double g) {
            greeks.setGamma(g);
            return this;
        }

        public GreeksBuilder theta(double t) {
            greeks.setTheta(t);
            return this;
        }

        public GreeksBuilder vega(double v) {
            greeks.setVega(v);
            return this;
        }

        public GreeksBuilder rho(double r) {
            greeks.setRho(r);
            return this;
        }

        public GreeksBuilder iv(double i) {
            greeks.setIv(i);
            return this;
        }

        public Greeks build() {
            return greeks;
        }
    }
}
