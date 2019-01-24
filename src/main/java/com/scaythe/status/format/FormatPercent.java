package com.scaythe.status.format;

public class FormatPercent {

    private static final String PERCENT_FORMAT = "%3.0f%%";

    public static String format(double d) {
        return String.format(PERCENT_FORMAT, d * 100);
    }
}
