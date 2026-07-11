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

    /**
     * Computes the zig-zag placement order for the version described by {@code baseMatrixData}.
     * Like the base matrix itself, the result is data-independent, so callers generating many
     * symbols of the same version should compute it once and pass it to
     * {@link #generateMatrixData(MatrixData, int[], QRCodeVersion, ECCLevel, int[])}.
     */
    public static int[] computePlacementOrder(MatrixData baseMatrixData) {
        return DataMaskApplier.computePlacementOrder(baseMatrixData);
    }

    /** Variant of {@link #generateMatrixData(MatrixData, int[], QRCodeVersion, ECCLevel, int[])}
     * that derives the placement order on the fly, for one-off callers. */
    public static MatrixData generateMatrixData(MatrixData baseMatrixData, QRCodeVersion version, ECCLevel eccLevel,
            int[] inputData) {
        return generateMatrixData(baseMatrixData, computePlacementOrder(baseMatrixData), version, eccLevel, inputData);
    }

    public static MatrixData generateMatrixData(MatrixData baseMatrixData, int[] placementOrder,
            QRCodeVersion version, ECCLevel eccLevel, int[] inputData) {
        int versionValue = version.getValue();

        // A single scratch copy is reused across all mask trials: every module outside the
        // fixed function patterns is unconditionally rewritten on each trial — the data pass
        // assigns every unreserved module, and the format/version writers assign every module
        // of their areas — so no state leaks from one trial to the next. This replaces the
        // previous one-copy-per-mask approach (8 full matrix allocations per generation),
        // which dominated allocation pressure under high-throughput use.
        MatrixData scratch = new MatrixData(baseMatrixData);

        int bestMask = 0;
        int bestPenalty = Integer.MAX_VALUE;

        for (int mask = 0; mask < DataMaskApplier.MASK_PATTERN_COUNT; mask++) {
            writeMaskedSymbol(scratch, placementOrder, versionValue, eccLevel, inputData, mask);

            // Passing the best score so far as cutoff lets the calculator abort a trial as soon
            // as it is provably worse than the current winner (penalties only accumulate).
            int penalty = PenaltyCalculator.calculate(scratch.matrix(), bestPenalty);
            if (penalty < bestPenalty) {
                bestPenalty = penalty;
                bestMask = mask;
            }
        }

        // The scratch matrix currently holds the last trial; reapply the winner unless it
        // already is the last mask.
        if (bestMask != DataMaskApplier.MASK_PATTERN_COUNT - 1) {
            writeMaskedSymbol(scratch, placementOrder, versionValue, eccLevel, inputData, bestMask);
        }
        return scratch;
    }

    /** Writes the complete symbol for one mask trial: masked data, format and version information. */
    private static void writeMaskedSymbol(MatrixData matrixData, int[] placementOrder, int versionValue,
            ECCLevel eccLevel, int[] inputData, int mask) {
        DataMaskApplier.applyDataAndMask(matrixData, inputData, mask, placementOrder);
        FormatInformation.write(matrixData, eccLevel, mask);
        if (versionValue >= VERSION_INFO_MIN_VERSION) {
            VersionInformation.write(matrixData, versionValue);
        }
    }
}
