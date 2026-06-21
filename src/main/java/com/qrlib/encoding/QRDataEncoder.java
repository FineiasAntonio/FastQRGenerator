package com.qrlib.encoding;

import com.qrlib.config.QRCodeCapacity;

/**
 * Encodes an input string into the final interleaved codeword stream for a QR symbol, wiring
 * together data-codeword formatting, Reed-Solomon error correction and block interleaving
 * (ISO 18004).
 */
public class QRDataEncoder {

    private final DataCodewordFormatter formatter;
    private final CodewordInterleaver interleaver;

    public QRDataEncoder(QRCodeCapacity capacity, int version) {
        this.formatter = new DataCodewordFormatter(capacity, version);
        ReedSolomonEncoder reedSolomonEncoder = new ReedSolomonEncoder(capacity.getEcPerBlock());
        this.interleaver = new CodewordInterleaver(capacity, reedSolomonEncoder);
    }

    public int[] encode(String data) {
        int[] dataCodewords = formatter.format(data);
        return interleaver.interleave(dataCodewords);
    }
}
