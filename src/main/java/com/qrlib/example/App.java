package com.qrlib.example;

import com.qrlib.QRCode;
import com.qrlib.QRCodeGenerator;
import com.qrlib.QRCodeGeneratorBuilder;
import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeVersion;

public class App {
    public static void main(String[] args) {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V1)
                .ECCLevel(ECCLevel.M)
                .build();

        QRCode code = generator.generate("Hello");
        code.print();
    }
}
