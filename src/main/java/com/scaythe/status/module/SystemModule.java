package com.scaythe.status.module;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

public class SystemModule extends SamplingModule<SystemData> {

    private static final String NAME = "system";
    private static final int SAMPLE_SIZE = 10;

    private final SystemInfo systemInfo = new SystemInfo();

    public SystemModule(Runnable update) {
        this(null, update);
    }

    public SystemModule(String instance, Runnable update) {
        super(Duration.ofSeconds(1), SAMPLE_SIZE, NAME, instance, update);
    }

    @Override
    public SystemData sample() {
        return new SystemData(cpu(), memory(), swap(), netDown(), netUp());
    }

    private double cpu() {
        return systemInfo.getHardware().getProcessor().getSystemCpuLoad();
    }

    private double memory() {
        long total = systemInfo.getHardware().getMemory().getTotal();
        long free = systemInfo.getHardware().getMemory().getAvailable();

        long used = total - free;

        return (double) used / (double) total;
    }

    private double swap() {
        long total = systemInfo.getHardware().getMemory().getSwapTotal();
        long used = systemInfo.getHardware().getMemory().getSwapUsed();

        return (double) used / (double) total;
    }

    private long netDown() {
        return Stream.of(systemInfo.getHardware().getNetworkIFs()).mapToLong(NetworkIF::getBytesRecv).sum();
    }

    private long netUp() {
        return Stream.of(systemInfo.getHardware().getNetworkIFs()).mapToLong(NetworkIF::getBytesSent).sum();
    }

    @Override
    public ModuleData reduce(List<SystemData> samples) {
        SystemData avg = reduceData(samples);

        return new ModuleData(MessageFormat.format("\uF2DB {0} \uF0C9 {1} \uF0EC {2} \uF019 {3} \uF093 {4}",
                percent(avg.cpu()),
                percent(avg.memory()),
                percent(avg.swap()),
                avgBytes(avg.netDown()),
                avgBytes(avg.netUp())));
    }

    private String percent(double d) {
        return formatPercent(d);
    }

    private String avgBytes(long bytes) {
        return formatBytes((double) bytes / (double) SAMPLE_SIZE);
    }

    private SystemData reduceData(List<SystemData> samples) {
        return new SystemData(average(samples, SystemData::cpu),
                average(samples, SystemData::memory),
                average(samples, SystemData::swap),
                difference(samples, SystemData::netDown),
                difference(samples, SystemData::netUp));
    }

    private double average(List<SystemData> samples, ToDoubleFunction<SystemData> getter) {
        return samples.stream().mapToDouble(getter).average().orElse(0);
    }

    private long difference(List<SystemData> samples, ToLongFunction<SystemData> getter) {
        return getter.applyAsLong(samples.get(samples.size() - 1)) - getter.applyAsLong(samples.get(0));
    }

    private static final String PERCENT_FORMAT = "%6.2f%%";

    private static String formatPercent(double d) {
        return String.format(PERCENT_FORMAT, d * 100);
    }

    // https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    private static final String[] BYTE_UNITS = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
    private static final double BYTE_BASE = 1024d;
    private static final String BYTE_FORMAT = "%6.1f %-3s";

    private static String formatBytes(double bytes) {
        // When using the smallest unit no decimal point is needed, because it's the exact number.
        if (bytes < BYTE_BASE) {
            return String.format(BYTE_FORMAT, bytes, BYTE_UNITS[0]);
        }

        final int exponent = (int) (Math.log(bytes) / Math.log(BYTE_BASE));
        final String unit = BYTE_UNITS[exponent];
        return String.format(BYTE_FORMAT, bytes / Math.pow(BYTE_BASE, exponent), unit);
    }
}
