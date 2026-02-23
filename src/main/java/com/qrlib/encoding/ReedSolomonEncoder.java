package com.qrlib.encoding;

import com.qrlib.config.QRCodeCapacity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ReedSolomonEncoder {

    private static final String BYTE_MODE_INDICATOR = "0100";
    private static final int BITS_PER_BYTE = 8;
    private static final int PADDING_CODEWORD_A = 0xEC;
    private static final int PADDING_CODEWORD_B = 0x11;

    private final QRCodeCapacity capacity;
    private final int version;
    private final GFPolynomial generatorPolynomial;

    public ReedSolomonEncoder(QRCodeCapacity capacity, int version) {
        this.capacity = capacity;
        this.version = version;
        this.generatorPolynomial = createGeneratorPolynomial(capacity.getEcPerBlock());
    }

    private GFPolynomial createGeneratorPolynomial(int ecCWCount) {
        GFPolynomial generator = new GFPolynomial(new int[] { 1 });
        for (int i = 0; i < ecCWCount; i++) {
            GFPolynomial term = new GFPolynomial(new int[] { 1, GFPolynomial.getEXP()[i] });
            generator = generator.multiply(term);
        }
        return generator;
    }

    public int[] encode(String data) {
        int[] dataCodewords = formatInputData(data);

        // 1. Split data codewords into blocks
        int g1Blocks = capacity.getGroup1Blocks();
        int g1Data = capacity.getGroup1DataCodewords();
        int g2Blocks = capacity.getGroup2Blocks();
        int g2Data = capacity.getGroup2DataCodewords();
        int ecPerBlock = capacity.getEcPerBlock();
        int totalBlocks = g1Blocks + g2Blocks;

        int[][] dataBlocks = new int[totalBlocks][];
        int offset = 0;

        // Group 1 blocks
        for (int i = 0; i < g1Blocks; i++) {
            dataBlocks[i] = new int[g1Data];
            System.arraycopy(dataCodewords, offset, dataBlocks[i], 0, g1Data);
            offset += g1Data;
        }

        // Group 2 blocks
        for (int i = 0; i < g2Blocks; i++) {
            dataBlocks[g1Blocks + i] = new int[g2Data];
            System.arraycopy(dataCodewords, offset, dataBlocks[g1Blocks + i], 0, g2Data);
            offset += g2Data;
        }

        // 2. RS-encode each block independently
        int[][] ecBlocks = new int[totalBlocks][];
        for (int i = 0; i < totalBlocks; i++) {
            ecBlocks[i] = computeECCodewords(dataBlocks[i], ecPerBlock);
        }

        // 3. Interleave data codewords
        List<Integer> result = new ArrayList<>();

        int maxDataLen = Math.max(g1Data, g2Data);
        for (int col = 0; col < maxDataLen; col++) {
            for (int block = 0; block < totalBlocks; block++) {
                if (col < dataBlocks[block].length) {
                    result.add(dataBlocks[block][col]);
                }
            }
        }

        // 4. Interleave EC codewords
        for (int col = 0; col < ecPerBlock; col++) {
            for (int block = 0; block < totalBlocks; block++) {
                result.add(ecBlocks[block][col]);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }

    private int[] computeECCodewords(int[] dataBlock, int ecCount) {
        int[] paddedCoefficients = new int[dataBlock.length + ecCount];
        System.arraycopy(dataBlock, 0, paddedCoefficients, 0, dataBlock.length);

        GFPolynomial dataPoly = new GFPolynomial(paddedCoefficients);
        GFPolynomial remainder = dataPoly.getRemainder(generatorPolynomial);

        return remainder.getCoefficients();
    }

    private int[] formatInputData(String data) {
        int totalDataCapacity = capacity.getTotalDataCodewords();
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
