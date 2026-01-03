package com.qrlib;

import java.util.Arrays;

public class GFPolynomial {
    private final int[] coefficients;

    private static final int[] EXP = new int[512];
    private static final int[] LOG = new int[256];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP[i] = x;
            LOG[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0) {
                x ^= 0x11D;
            }
        }
        for (int i = 255; i < 512; i++) {
            EXP[i] = EXP[i - 255];
        }
    }

    public GFPolynomial(int[] coefficients) {
        this.coefficients = coefficients;
    }

    public double evaluate(double x) {
        double result = 0.0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }

    public GFPolynomial add(GFPolynomial p) {
        int maxDegree = Math.max(this.coefficients.length, p.coefficients.length);
        int[] resultCoeffs = new int[maxDegree];

        for (int i = 0; i < maxDegree; i++) {
            int coeff1 = i < this.coefficients.length ? this.coefficients[i] : 0;
            int coeff2 = i < p.coefficients.length ? p.coefficients[i] : 0;
            resultCoeffs[i] = coeff1 ^ coeff2;
        }

        return new GFPolynomial(resultCoeffs);
    }

    public GFPolynomial getRemainder(GFPolynomial divisor) {
        int[] out = Arrays.copyOf(this.coefficients, this.coefficients.length);

        for (int i = 0; i <= out.length - divisor.coefficients.length; i++) {
            int coef = out[i];
            if (coef != 0) {
                for (int j = 1; j < divisor.coefficients.length; j++) {
                    if (divisor.coefficients[j] != 0) {
                        out[i + j] ^= multiplyGF(divisor.coefficients[j], coef);
                    }
                }
            }
        }
        int remainderSize = divisor.coefficients.length - 1;
        return new GFPolynomial(Arrays.copyOfRange(out, out.length - remainderSize, out.length));
    }

    private static int multiplyGF(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return EXP[LOG[a] + LOG[b]];
    }

    public int[] getCoefficients() {
        return coefficients;
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
