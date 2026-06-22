package com.qrlib.encoding;

/**
 * Computes the Reed-Solomon error-correction codewords for a single data block, using a
 * generator polynomial over GF(256) (ISO 18004).
 */
public class ReedSolomonEncoder {

    private final int ecPerBlock;
    private final GFPolynomial generatorPolynomial;

    public ReedSolomonEncoder(int ecPerBlock) {
        this.ecPerBlock = ecPerBlock;
        this.generatorPolynomial = GFPolynomial.generator(ecPerBlock);
    }

    public int[] computeEcCodewords(int[] dataBlock) {
        int[] paddedCoefficients = new int[dataBlock.length + ecPerBlock];
        System.arraycopy(dataBlock, 0, paddedCoefficients, 0, dataBlock.length);

        GFPolynomial dataPoly = new GFPolynomial(paddedCoefficients);
        GFPolynomial remainder = dataPoly.getRemainder(generatorPolynomial);

        return remainder.getCoefficients();
    }
}
