package com.qrlib;

import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Golden snapshot of the generated QR matrices. Locks the current bit-for-bit output so the
 * DRY/SOLID refactoring can be verified to preserve behavior exactly. Each entry hashes the
 * full matrix (SHA-256 over the concatenated 0/1 rows) for a version/ECC combination chosen
 * to exercise the critical spec branches:
 * <ul>
 *   <li>V1/L  — minimum symbol, no alignment, no version info</li>
 *   <li>V2/M  — first alignment pattern</li>
 *   <li>V7/Q  — version information area appears (version >= 7)</li>
 *   <li>V10/H — character-count indicator switches from 8 to 16 bits (version >= 10)</li>
 *   <li>V40/H — maximum symbol</li>
 * </ul>
 */
class CharacterizationTest {

    // 15 bytes — fits every version/ECC combination below, including V1/L (max 17 bytes).
    private static final String PAYLOAD = "FastQRGenerator";

    private static final Map<String, String> GOLDEN = new LinkedHashMap<>();
    static {
        GOLDEN.put("V1/L", "9ca0d4ab854745aac18cd9fac12acf81408982eb886192de6a1ba3ca44701381");
        GOLDEN.put("V2/M", "5c438367f2332fcf4d26c4dbed4af0ec6d846de32f74d23e4c9c58a88e9358cf");
        GOLDEN.put("V7/Q", "453e71dbe20e5a10723a8a5dc7078db4c998fe05f7ce51ca9624817772fd1b39");
        GOLDEN.put("V10/H", "b01df751d3c5f3da1f1166fd1ea0aa605278bef0d58fee5a30d75fd2251e586f");
        GOLDEN.put("V40/H", "10f2c7413be46033f1d3e60e721b436a647d44f4750f63822800ee3a85e8d7d5");
    }

    @Test
    void generatedMatricesMatchGoldenSnapshot() {
        assertEquals(GOLDEN.get("V1/L"), hashFor(QRCodeVersion.V1, ECCLevel.L));
        assertEquals(GOLDEN.get("V2/M"), hashFor(QRCodeVersion.V2, ECCLevel.M));
        assertEquals(GOLDEN.get("V7/Q"), hashFor(QRCodeVersion.V7, ECCLevel.Q));
        assertEquals(GOLDEN.get("V10/H"), hashFor(QRCodeVersion.V10, ECCLevel.H));
        assertEquals(GOLDEN.get("V40/H"), hashFor(QRCodeVersion.V40, ECCLevel.H));
    }

    private static String hashFor(QRCodeVersion version, ECCLevel eccLevel) {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(version)
                .ECCLevel(eccLevel)
                .build();
        byte[][] matrix = generator.generate(PAYLOAD).getMatrixData().getMatrix();
        return matrixHash(matrix);
    }

    private static String matrixHash(byte[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (byte[] row : matrix) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(sb.toString().getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
