package com.qrlib;

import com.qrlib.dev.QRCode;
import com.qrlib.dev.QRCodeGenerator;

import java.util.Arrays;

public class App {
    public static void main( String[] args ) {
        QRCodeGenerator generator = new QRCodeGenerator();

        QRCode code = generator.generate();
    }
}
