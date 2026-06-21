package com.qrlib.encoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GFPolynomialTest {

    @Test
    void expAndLogTablesAreInverses() {
        for (int i = 0; i < 255; i++) {
            assertEquals(i, GFPolynomial.log(GFPolynomial.exp(i)), "log(exp(" + i + ")) should be " + i);
        }
    }

    @Test
    void expTableFollowsQrPrimitivePolynomial0x11D() {
        // alpha^0 .. alpha^7 are plain powers of two
        for (int i = 0; i <= 7; i++) {
            assertEquals(1 << i, GFPolynomial.exp(i));
        }

        // alpha^8 wraps around: 0x100 ^ 0x11D = 0x1D
        assertEquals(0x1D, GFPolynomial.exp(8));
    }

    @Test
    void expTableRepeatsWithPeriod255() {
        for (int i = 0; i < 255; i++) {
            assertEquals(GFPolynomial.exp(i), GFPolynomial.exp(i + 255));
        }
    }

    @Test
    void multiplyByOneIsIdentity() {
        GFPolynomial poly = new GFPolynomial(new int[] { 5, 12, 200 });
        GFPolynomial identity = new GFPolynomial(new int[] { 1 });

        assertArrayEquals(new int[] { 5, 12, 200 }, poly.multiply(identity).getCoefficients());
    }

    @Test
    void multiplyByZeroIsZero() {
        GFPolynomial poly = new GFPolynomial(new int[] { 5, 12, 200 });
        GFPolynomial zero = new GFPolynomial(new int[] { 0 });

        assertArrayEquals(new int[] { 0, 0, 0 }, poly.multiply(zero).getCoefficients());
    }

    @Test
    void addingPolynomialToItselfYieldsZero() {
        GFPolynomial poly = new GFPolynomial(new int[] { 17, 200, 3 });

        assertArrayEquals(new int[] { 0, 0, 0 }, poly.add(poly).getCoefficients());
    }

    @Test
    void remainderHasOneLessDegreeThanDivisor() {
        GFPolynomial dividend = new GFPolynomial(new int[] { 1, 2, 3, 4, 5, 0, 0 });
        GFPolynomial divisor = new GFPolynomial(new int[] { 1, 1, 1 });

        GFPolynomial remainder = dividend.getRemainder(divisor);

        assertEquals(divisor.getCoefficients().length - 1, remainder.getCoefficients().length);
    }
}
