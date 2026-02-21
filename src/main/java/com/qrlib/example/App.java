package com.qrlib.example;

import com.qrlib.QRCode;
import com.qrlib.QRCodeGenerator;
import com.qrlib.QRCodeGeneratorBuilder;
import com.qrlib.config.ECCLevel;
import com.qrlib.config.QRCodeSize;
import com.qrlib.config.QRCodeVersion;

public class App {
    public static void main(String[] args) {
        QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                .version(QRCodeVersion.V2)
                .ECCLevel(ECCLevel.L)
                .build();

        QRCode code = generator.generate("https://www.google.com");
        code.print();
    }
}
