package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.EncodingMode;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.config.QRCodeVersion;
import com.qrlib.config.VersionSelector;
import com.qrlib.encoding.QRDataEncoder;
import com.qrlib.matrix.MatrixData;
import com.qrlib.matrix.MatrixDataGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates QR symbols from text payloads per ISO/IEC 18004. Payloads consisting only of
 * ASCII digits are automatically encoded in numeric mode (3 digits per 10 bits); everything
 * else is encoded in byte mode (UTF-8). Instances are created through
 * {@link QRCodeGeneratorBuilder} and hold the configured symbol version (or automatic
 * selection) and error-correction level.
 * <p>
 * This class is thread-safe: a single instance can be shared across threads and
 * {@link #generate(String)} may be called concurrently. Per-version setup (base matrix,
 * placement order, encoder) is computed once and cached, so reusing one generator for many
 * payloads is cheaper than creating a new one per call.
 */
public class QRCodeGenerator {

    private final QRCodeVersion fixedVersion; // null => smallest fitting version is chosen per payload
    private final ECCLevel eccLevel;
    private final Map<QRCodeVersion, Pipeline> pipelines = new ConcurrentHashMap<>();

    QRCodeGenerator(QRCodeVersion fixedVersion, ECCLevel eccLevel) {
        this.fixedVersion = fixedVersion;
        this.eccLevel = eccLevel;
    }

    /**
     * Encodes the given text as a QR symbol, in numeric mode when the payload is all ASCII
     * digits and byte mode (UTF-8) otherwise.
     *
     * @param data the payload
     * @return the generated symbol, ready for rendering
     * @throws IllegalArgumentException if the payload does not fit the configured (or largest)
     *         version at the configured error-correction level
     */
    public QRCode generate(String data) {
        EncodingMode mode = EncodingMode.detect(data);
        byte[] payload = mode == EncodingMode.BYTE ? data.getBytes(StandardCharsets.UTF_8) : null;
        int characterCount = mode == EncodingMode.BYTE ? payload.length : data.length();
        QRCodeVersion version = resolveVersion(characterCount, mode);

        Pipeline pipeline = pipelines.computeIfAbsent(version, v -> Pipeline.create(v, eccLevel));
        int[] encodedData = mode == EncodingMode.NUMERIC
                ? pipeline.encoder.encodeNumeric(data)
                : pipeline.encoder.encode(payload);
        MatrixData matrixData = MatrixDataGenerator.generateMatrixData(pipeline.baseMatrix,
                pipeline.placementOrder, version, eccLevel, encodedData);
        return new QRCode(matrixData);
    }

    private QRCodeVersion resolveVersion(int characterCount, EncodingMode mode) {
        if (fixedVersion == null) {
            return VersionSelector.smallestFor(characterCount, mode, eccLevel);
        }
        if (!VersionSelector.fits(characterCount, mode, fixedVersion, eccLevel)) {
            String unit = mode == EncodingMode.NUMERIC ? "digits" : "bytes";
            int capacity = mode == EncodingMode.NUMERIC
                    ? VersionSelector.numericCapacity(fixedVersion, eccLevel)
                    : VersionSelector.byteCapacity(fixedVersion, eccLevel);
            throw new IllegalArgumentException("Payload of " + characterCount + " " + unit + " does not fit in "
                    + fixedVersion + " at ECC level " + eccLevel + " (max " + capacity + " " + unit + ")");
        }
        return fixedVersion;
    }

    /**
     * Reusable per-version setup: the base matrix (function patterns placed, never written to
     * after creation — {@link MatrixDataGenerator#generateMatrixData} copies it before writing),
     * the zig-zag placement order derived from it, and the data encoder.
     */
    private static final class Pipeline {
        final MatrixData baseMatrix;
        final int[] placementOrder;
        final QRDataEncoder encoder;

        private Pipeline(MatrixData baseMatrix, int[] placementOrder, QRDataEncoder encoder) {
            this.baseMatrix = baseMatrix;
            this.placementOrder = placementOrder;
            this.encoder = encoder;
        }

        static Pipeline create(QRCodeVersion version, ECCLevel eccLevel) {
            MatrixData baseMatrix = MatrixDataGenerator.createBaseMatrix(version);
            int[] placementOrder = MatrixDataGenerator.computePlacementOrder(baseMatrix);
            QRCodeCapacity capacity = QRCodeCapacity.getCapacity(version, eccLevel);
            QRDataEncoder encoder = new QRDataEncoder(capacity, version.getValue());
            return new Pipeline(baseMatrix, placementOrder, encoder);
        }
    }
}
