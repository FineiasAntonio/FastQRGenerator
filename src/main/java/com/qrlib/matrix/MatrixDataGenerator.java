package com.qrlib.matrix;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;

import java.util.List;

/**
 * Orchestrates matrix construction. Pattern placement, data masking, format/version information
 * and penalty scoring each live in their own collaborator; this class wires them together:
 * it builds the data bitstream, tries every mask pattern, and keeps the one with the lowest
 * penalty (ISO 18004).
 */
public class MatrixDataGenerator {

    private MatrixDataGenerator() {
    }

    private static final int VERSION_INFO_MIN_VERSION = 7;

    public static void placeCommonPatterns(MatrixData matrixData, QRCodeVersion version) {
        StructuralPatternPlacer.placeCommonPatterns(matrixData, version);
    }

    public static MatrixData generateMatrixData(MatrixData baseMatrixData, QRCodeVersion version, ECCLevel eccLevel,
            int[] inputData) {
        int versionValue = version.getValue();
        List<Integer> bitstream = DataMaskApplier.buildBitstream(inputData, versionValue);

        MatrixData bestMatrixData = null;
        int bestPenalty = Integer.MAX_VALUE;

        for (int mask = 0; mask < DataMaskApplier.MASK_PATTERN_COUNT; mask++) {
            MatrixData matrixData = new MatrixData(baseMatrixData);
            DataMaskApplier.applyDataAndMask(matrixData, bitstream, mask);

            FormatInformation.write(matrixData, eccLevel, mask);

            if (versionValue >= VERSION_INFO_MIN_VERSION) {
                VersionInformation.write(matrixData, versionValue);
            }

            int penalty = PenaltyCalculator.calculate(matrixData.getMatrix());
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestMatrixData = matrixData;
            }
        }

        return bestMatrixData;
    }
}
