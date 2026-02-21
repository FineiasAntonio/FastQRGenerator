package com.qrlib.encoding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReedSolomonEncoder {

    private int numberOfECCodewords = 10; // V1
    private final int totalDataCapacity = 16; // V1
    private GFPolynomial generatorPolynomial;

    public ReedSolomonEncoder() {
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

        bits.append("0100"); // Byte mode indicator

        String lengthBits = Integer.toBinaryString(rawBytes.length);
        while (lengthBits.length() < 8)
            lengthBits = "0" + lengthBits;
        bits.append(lengthBits);

        for (byte b : rawBytes) {
            String bBits = Integer.toBinaryString(Byte.toUnsignedInt(b));
            while (bBits.length() < 8)
                bBits = "0" + bBits;
            bits.append(bBits);
        }

        bits.append("0000"); // Byte mode indicator end

        while (bits.length() % 8 != 0)
            bits.append("0"); // Round to the nearest byte

        List<Integer> codewords = new ArrayList<>();
        for (int i = 0; i < bits.length(); i += 8) {
            codewords.add(Integer.parseInt(bits.substring(i, i + 8), 2));
        }

        // Padding
        boolean toggle = true;
        while (codewords.size() < totalDataCapacity) {
            codewords.add(toggle ? 0xEC : 0x11);
            toggle = !toggle;
        }

        return codewords.stream().mapToInt(i -> i).toArray();
    }
}
