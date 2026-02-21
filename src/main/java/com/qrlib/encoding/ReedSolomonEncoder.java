package com.qrlib.encoding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReedSolomonEncoder {

    private static final String BYTE_MODE_INDICATOR = "0100";
    private static final String TERMINATOR = "0000";
    private static final int BITS_PER_BYTE = 8;
    private static final int PADDING_CODEWORD_A = 0xEC;
    private static final int PADDING_CODEWORD_B = 0x11;

    private final int numberOfECCodewords;
    private final int totalDataCapacity;
    private final int version;
    private GFPolynomial generatorPolynomial;

    public ReedSolomonEncoder(int totalDataCapacity, int numberOfECCodewords, int version) {
        this.totalDataCapacity = totalDataCapacity;
        this.numberOfECCodewords = numberOfECCodewords;
        this.version = version;
        createGeneratorPolynomial();
    }

    private void createGeneratorPolynomial() {
        GFPolynomial generator = new GFPolynomial(new int[] { 1 });
        for (int i = 0; i < this.numberOfECCodewords; i++) {
            GFPolynomial term = new GFPolynomial(new int[] { 1, GFPolynomial.getEXP()[i] });
            generator = generator.multiply(term);
        }
        this.generatorPolynomial = generator;
    }

    public int[] encode(String data) {
        int[] dataCodewords = formatInputData(data);

        int[] paddedCoefficients = new int[dataCodewords.length + numberOfECCodewords];
        System.arraycopy(dataCodewords, 0, paddedCoefficients, 0, dataCodewords.length);

        GFPolynomial dataPoly = new GFPolynomial(paddedCoefficients);
        GFPolynomial ECC = dataPoly.getRemainder(generatorPolynomial);

        int[] encodedData = new int[dataCodewords.length + numberOfECCodewords];
        System.arraycopy(dataCodewords, 0, encodedData, 0, dataCodewords.length);
        System.arraycopy(ECC.getCoefficients(), 0, encodedData, dataCodewords.length, numberOfECCodewords);
        return encodedData;
    }

    private int[] formatInputData(String data) {
        byte[] rawBytes = data.getBytes(StandardCharsets.UTF_8);
        StringBuilder bits = new StringBuilder();

        bits.append(BYTE_MODE_INDICATOR);

        int characterCountLength = version < 10 ? 8 : 16;
        String lengthBits = Integer.toBinaryString(rawBytes.length);
        while (lengthBits.length() < characterCountLength)
            lengthBits = "0" + lengthBits;
        bits.append(lengthBits);

        for (byte b : rawBytes) {
            String bBits = Integer.toBinaryString(Byte.toUnsignedInt(b));
            while (bBits.length() < BITS_PER_BYTE)
                bBits = "0" + bBits;
            bits.append(bBits);
        }

        // bits.append(TERMINATOR);
        int remainBits = (totalDataCapacity * BITS_PER_BYTE) - bits.length();
        int terminatorSize = Math.min(4, remainBits);

        for (int i = 0; i < terminatorSize; i++) {
            bits.append("0");
        }

        while (bits.length() % BITS_PER_BYTE != 0)
            bits.append("0");

        List<Integer> codewords = new ArrayList<>();
        for (int i = 0; i < bits.length(); i += BITS_PER_BYTE) {
            codewords.add(Integer.parseInt(bits.substring(i, i + BITS_PER_BYTE), 2));
        }

        boolean toggle = true;
        while (codewords.size() < totalDataCapacity) {
            codewords.add(toggle ? PADDING_CODEWORD_A : PADDING_CODEWORD_B);
            toggle = !toggle;
        }

        return codewords.stream().mapToInt(i -> i).toArray();
    }
}
