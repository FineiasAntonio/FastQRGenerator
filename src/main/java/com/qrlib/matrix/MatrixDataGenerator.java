package com.qrlib.matrix;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;

/**
 * Orchestrates matrix construction. Pattern placement, data masking, format/version information
 * and penalty scoring each live in their own collaborator; this class wires them together:
 * it places the codeword stream under every mask pattern and keeps the one with the lowest
 * penalty (ISO 18004).
 */
public class MatrixDataGenerator {

    private MatrixDataGenerator() {
    }

    private static final int VERSION_INFO_MIN_VERSION = 7;

    // Version 1 is 21x21; each subsequent version adds 4 modules per side (ISO 18004).
    private static final int VERSION_1_SIZE = 21;
    private static final int SIZE_INCREMENT_PER_VERSION = 4;

    /**
     * Builds the base matrix for a version: the fixed function patterns placed and the
     * format/version information areas reserved. The result is independent of the encoded
     * data, so it can be built once per version and shared across generations —
     * {@link #generateMatrixData} copies it before writing.
     */
    public static MatrixData createBaseMatrix(QRCodeVersion version) {
        int size = VERSION_1_SIZE + (version.getValue() - 1) * SIZE_INCREMENT_PER_VERSION;
        MatrixData matrixData = new MatrixData(size);
        StructuralPatternPlacer.placeCommonPatterns(matrixData, version);
        return matrixData;
    }

    public static MatrixData generateMatrixData(MatrixData baseMatrixData, QRCodeVersion version, ECCLevel eccLevel,
            int[] inputData) {
        int versionValue = version.getValue();

        // Every trial rewrites all mask-dependent modules unconditionally — the zig-zag
        // placement covers every unreserved module and the format/version writers cover their
        // whole areas — so two buffers suffice for the eight trials: the current trial simply
        // overwrites whatever the previous one left behind, and the best result so far is
        // parked in the other buffer.
        MatrixData currentMatrixData = new MatrixData(baseMatrixData);
        MatrixData bestMatrixData = null;
        int bestPenalty = Integer.MAX_VALUE;

        for (int mask = 0; mask < DataMaskApplier.MASK_PATTERN_COUNT; mask++) {
            DataMaskApplier.applyDataAndMask(currentMatrixData, inputData, mask);

            FormatInformation.write(currentMatrixData, eccLevel, mask);

            if (versionValue >= VERSION_INFO_MIN_VERSION) {
                VersionInformation.write(currentMatrixData, versionValue);
            }

            int penalty = PenaltyCalculator.calculate(currentMatrixData.getMatrix());
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                MatrixData previousBest = bestMatrixData;
                bestMatrixData = currentMatrixData;
                currentMatrixData = (previousBest != null) ? previousBest : new MatrixData(baseMatrixData);
            }
        }

        return bestMatrixData;
    }
}
