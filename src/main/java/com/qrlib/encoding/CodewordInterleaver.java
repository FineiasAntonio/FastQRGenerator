package com.qrlib.encoding;

import com.qrlib.config.QRCodeCapacity;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the data codewords into the version's error-correction blocks, computes each block's
 * Reed-Solomon codewords, and interleaves data and EC codewords into the final stream order
 * required by ISO 18004.
 */
public class CodewordInterleaver {

    private final QRCodeCapacity capacity;
    private final ReedSolomonEncoder reedSolomonEncoder;

    public CodewordInterleaver(QRCodeCapacity capacity, ReedSolomonEncoder reedSolomonEncoder) {
        this.capacity = capacity;
        this.reedSolomonEncoder = reedSolomonEncoder;
    }

    public int[] interleave(int[] dataCodewords) {
        int g1Blocks = capacity.getGroup1Blocks();
        int g1Data = capacity.getGroup1DataCodewords();
        int g2Blocks = capacity.getGroup2Blocks();
        int g2Data = capacity.getGroup2DataCodewords();
        int ecPerBlock = capacity.getEcPerBlock();
        int totalBlocks = g1Blocks + g2Blocks;

        int[][] dataBlocks = new int[totalBlocks][];
        int offset = 0;

        for (int i = 0; i < g1Blocks; i++) {
            dataBlocks[i] = new int[g1Data];
            System.arraycopy(dataCodewords, offset, dataBlocks[i], 0, g1Data);
            offset += g1Data;
        }

        for (int i = 0; i < g2Blocks; i++) {
            dataBlocks[g1Blocks + i] = new int[g2Data];
            System.arraycopy(dataCodewords, offset, dataBlocks[g1Blocks + i], 0, g2Data);
            offset += g2Data;
        }

        int[][] ecBlocks = new int[totalBlocks][];
        for (int i = 0; i < totalBlocks; i++) {
            ecBlocks[i] = reedSolomonEncoder.computeEcCodewords(dataBlocks[i]);
        }

        List<Integer> result = new ArrayList<>();

        int maxDataLen = Math.max(g1Data, g2Data);
        for (int col = 0; col < maxDataLen; col++) {
            for (int block = 0; block < totalBlocks; block++) {
                if (col < dataBlocks[block].length) {
                    result.add(dataBlocks[block][col]);
                }
            }
        }

        for (int col = 0; col < ecPerBlock; col++) {
            for (int block = 0; block < totalBlocks; block++) {
                result.add(ecBlocks[block][col]);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }
}
