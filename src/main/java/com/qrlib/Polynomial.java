package com.qrlib;

public class Polynomial {
    private final double[] coefficients;

    public Polynomial(double[] coefficients) {
        this.coefficients = coefficients;
    }

    public double evaluate(double x) {
        double result = 0.0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }

    public Polynomial add(Polynomial p) {
        int maxDegree = Math.max(this.coefficients.length, p.coefficients.length);
        double[] resultCoeffs = new double[maxDegree];

        for (int i = 0; i < maxDegree; i++) {
            double coeff1 = i < this.coefficients.length ? this.coefficients[i] : 0;
            double coeff2 = i < p.coefficients.length ? p.coefficients[i] : 0;
            resultCoeffs[i] = coeff1 + coeff2;
        }

        return new Polynomial(resultCoeffs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coefficients.length; i++) {
            if (i > 0 && coefficients[i] >= 0) {
                sb.append("+");
            }
            sb.append(coefficients[i]);
            if (i > 0) {
                sb.append("x");
                if (i > 1) {
                    sb.append("^").append(i);
                }
            }
        }
        return sb.toString();
    }
}
