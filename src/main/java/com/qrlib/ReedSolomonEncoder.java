package com.qrlib;

import java.nio.charset.StandardCharsets;

public class ReedSolomonEncoder {

    private int numberOfECCodewords = 2;
    private GFPolynomial generatorPolynomial;

    public ReedSolomonEncoder(int numberOfECCodewords) {
        this.numberOfECCodewords = numberOfECCodewords;
        createGeneratorPolynomial();
    }

    public void setNumberOfECCodewords(int numberOfECCodewords) {
        this.numberOfECCodewords = numberOfECCodewords;
        createGeneratorPolynomial();
    }

    private void createGeneratorPolynomial() {
        GFPolynomial generator = new GFPolynomial(new int[]{1});
        for (int i = 0; i < this.numberOfECCodewords; i++) {
            GFPolynomial term = new GFPolynomial(new int[]{1, GFPolynomial.getEXP()[i]});
            generator = generator.multiply(term);
        }
        this.generatorPolynomial = generator;
    }

    public int[] encode(String data) {
        int[] dataCoefficients = getDataCoefficients(data);

        int[] paddedCoefficients = new int[dataCoefficients.length + numberOfECCodewords];
        System.arraycopy(dataCoefficients, 0, paddedCoefficients, 0, dataCoefficients.length);

        GFPolynomial dataPoly = new GFPolynomial(paddedCoefficients);
        GFPolynomial ECC = dataPoly.getRemainder(generatorPolynomial);

        int[] encodedData = new int[dataCoefficients.length + numberOfECCodewords];
        System.arraycopy(dataCoefficients, 0, encodedData, 0, dataCoefficients.length);
        System.arraycopy(ECC.getCoefficients(), 0, encodedData, dataCoefficients.length, numberOfECCodewords);
        return encodedData;
    }

    private int[] getDataCoefficients(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        int[] coefficients = new int[dataBytes.length];
        for (int i = 0; i < dataBytes.length; i++) {
            coefficients[i] = Byte.toUnsignedInt(dataBytes[i]);
        }
        return coefficients;
    }

}
