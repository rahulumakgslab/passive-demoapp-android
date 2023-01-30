package com.sonde.mentalfitness.presentation.utils;

public class ArithmeticUtils {
    public static int getPercentage(int value1, int value2) {
        int percentage = 0;
        percentage = (int) ((value1 *100 ) / value2);
        return percentage;
    }
}
