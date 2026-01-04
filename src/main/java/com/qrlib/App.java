package com.qrlib;

import java.util.Arrays;

public class App
{
    public static void main( String[] args ) {
        ReedSolomonEncoder encoder = new ReedSolomonEncoder(4);

        int[] encodedMessage = encoder.encode("Teste");

        System.out.println(Arrays.toString(encodedMessage) + " " + new String(encodedMessage, 0, 5));

    }
}
