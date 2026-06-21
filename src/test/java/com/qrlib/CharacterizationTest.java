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

    private static final String PAYLOAD = "https://github.com/FineiasAntonio/FastQRGenerator";

    private static final Map<String, String> GOLDEN = new LinkedHashMap<>();
    static {
        GOLDEN.put("V1/L", "80054b68e2bef591a89b9c5f593f0ff8e7824c47674112d2876ae9b80d6ace3f");
        GOLDEN.put("V2/M", "0d3b4b94081d3e3e6d2ee9fa2fbffd1aa162a6fd7003571a9c0f5fd7605c1e7b");
        GOLDEN.put("V7/Q", "8a76d56a7ecc679c924290d08442093c9f7e18729e9fd3d94ee77a1806ba1949");
        GOLDEN.put("V10/H", "ac8838c4f4f75913a3ffa46cd2cb0e4097b6d5927796cbb9b3b2447528548635");
        GOLDEN.put("V40/H", "f46084d048e1739a450036d9f611c24063e0113fda23f94a603c1fb073f96c8a");
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
        int[][] matrix = generator.generate(PAYLOAD).getMatrixData().getMatrix();
        return matrixHash(matrix);
    }

    private static String matrixHash(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : matrix) {
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
