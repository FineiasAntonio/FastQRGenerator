package com.qrlib;

public class App 
{
    public static void main( String[] args ) {
        Polynomial p = new Polynomial(new double[]{2, 0, -3});
        Polynomial p2 = new Polynomial(new double[]{2, 0, 3});

        System.out.println("Polynomial: " + p);
        System.out.println("p(2) = " + p.evaluate(2));

        Polynomial sum = p.add(p2);
        System.out.println("Sum: " + sum);
    }
}
