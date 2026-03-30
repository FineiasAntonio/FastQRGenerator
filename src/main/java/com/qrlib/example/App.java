package com.qrlib.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.qrlib.QRCode;
import com.qrlib.QRCodeGenerator;
import com.qrlib.QRCodeGeneratorBuilder;
import com.qrlib.config.ECCLevel;
import com.qrlib.config.ImageExtensions;
import com.qrlib.config.QRCodeSize;
import com.qrlib.config.QRCodeVersion;

public class App {
    public static void main(String[] args) throws IOException {
        // Testar versões que cobrem todos os thresholds de remainder bits:
        // V1 (0 bits), V3 (7 bits), V7 (0 bits), V14 (3 bits),
        // V21 (4 bits), V28 (3 bits), V35 (0 bits)
        QRCodeVersion[] versions = {
                QRCodeVersion.V1, QRCodeVersion.V3, QRCodeVersion.V7,
                QRCodeVersion.V14, QRCodeVersion.V21, QRCodeVersion.V28,
                QRCodeVersion.V35
        };

        String testData = "https://www.google.com";

        for (QRCodeVersion version : versions) {
            QRCodeGenerator generator = new QRCodeGeneratorBuilder()
                    .version(version)
                    .ECCLevel(ECCLevel.M)
                    .build();

            QRCode code = generator.generate(testData);
            String filename = "qrcode_" + version.name() + ".png";
            File file = new File(filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(code.getAsImage().toByteArray());
            fos.close();
            System.out.println("Generated: " + filename + " (version " + version.getValue() + ")");
        }

        System.out.println("\nAll QR codes generated successfully. Scan each to verify readability.");
    }
}

