package com.qrlib;

import java.util.Arrays;

public class App {
    public static void main( String[] args ) {
        ReedSolomonEncoder encoder = new ReedSolomonEncoder();

        int[] encodedMessage = encoder.encode("Teste");

        System.out.println(Arrays.toString(encodedMessage));

        MatrixData matrixData = MatrixDataGenerator.generateMatrixData(encodedMessage);
        matrixData.printImage();
        matrixData.printChars();
    }
}
