package com.scaythe.status.format;

public class FormatBytes {

    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private static final String[] BYTE_UNITS = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
    private static final double BYTE_BASE = 1024d;
    private static final String BYTE_FORMAT = "%6.1f %-5s";

    public static String format(double bytes) {
        final int exponent = exponent(bytes);
        final String unit = BYTE_UNITS[exponent];

        return String.format(BYTE_FORMAT, bytes / Math.pow(BYTE_BASE, exponent), unit + "/s");
    }

    private static int exponent(double bytes) {
        if (bytes < BYTE_BASE) {
            return 0;
        }

        return (int) (Math.log(bytes) / Math.log(BYTE_BASE));
    }
}
