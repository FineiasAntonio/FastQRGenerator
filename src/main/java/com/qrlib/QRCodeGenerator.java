package com.qrlib;

import com.qrlib.encoding.ReedSolomonEncoder;
import com.qrlib.matrix.MatrixData;
import com.qrlib.matrix.MatrixDataGenerator;
import com.qrlib.config.QRCodeCapacity;
import com.qrlib.template.QRCodeTemplate;

public class QRCodeGenerator {

    private final QRCodeTemplate template;
    private final ReedSolomonEncoder encoder;

    public QRCodeGenerator(QRCodeTemplate template) {
        this.template = template;
        QRCodeCapacity capacity = QRCodeCapacity.getCapacity(template.getVersion(), template.getEccLevel());
        this.encoder = new ReedSolomonEncoder(capacity.getTotalDataCodewords(), capacity.getEcCodewords(),
                template.getVersion().getValue());
    }

    public QRCode generate(String data) {
        QRCodeTemplate workingCopy = template.copy();
        int[] encodedData = encoder.encode(data);
        MatrixData matrixData = MatrixDataGenerator.generateMatrixData(workingCopy, encodedData);
        return new QRCode(matrixData);
    }
}
