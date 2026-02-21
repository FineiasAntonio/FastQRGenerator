package com.qrlib;

import com.qrlib.matrix.MatrixData;

public class QRCode {

    private final MatrixData matrixData;

    public QRCode(MatrixData matrixData) {
        this.matrixData = matrixData;
    }

    public MatrixData getMatrixData() {
        return matrixData;
    }

    public void print() {
        matrixData.printImage();
    }

    public void printChars() {
        matrixData.printChars();
    }
}
