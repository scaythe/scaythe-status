package com.scaythe.status.module;

import com.scaythe.status.format.FormatBytes;
import com.scaythe.status.format.FormatPercent;
import com.scaythe.status.markup.PangoMarkup;
import com.scaythe.status.module.sub.Submodule;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SystemModule extends SamplingModule<SystemData> {

    private static final String NAME = "system";
    private static final int SAMPLE_SIZE = 10;

    private final SystemInfo systemInfo = new SystemInfo();

    private final List<Submodule<SystemData, ?>> submodules = new ArrayList<>();

    public SystemModule(Runnable update) {
        this(null, update);
    }

    public SystemModule(String instance, Runnable update) {
        super(Duration.ofSeconds(1), SAMPLE_SIZE, NAME, instance, PangoMarkup.NAME, update);

        submodules.add(new Submodule<>("\uF2DB",
                SystemData::cpu,
                this::percent,
                this::percentColors));
        submodules.add(new Submodule<>("\uF0C9",
                SystemData::memory,
                this::percent,
                this::percentColors));
        submodules.add(new Submodule<>("\uF0EC",
                SystemData::swap,
                this::percent,
                this::swapColors));
        submodules.add(new Submodule<>("\uF019",
                SystemData::netDown,
                this::avgBytes,
                d -> Optional.empty()));
        submodules.add(new Submodule<>("\uF093",
                SystemData::netUp,
                this::avgBytes,
                d -> Optional.empty()));
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
        return Stream.of(systemInfo.getHardware().getNetworkIFs())
                .mapToLong(NetworkIF::getBytesRecv)
                .sum();
    }

    private long netUp() {
        return Stream.of(systemInfo.getHardware().getNetworkIFs())
                .mapToLong(NetworkIF::getBytesSent)
                .sum();
    }

    @Override
    public ModuleData reduce(List<SystemData> samples) {
        SystemData avg = reduceData(samples);

        String submodulesText = submodules.stream()
                .map(s -> s.format(avg))
                .collect(Collectors.joining("<span fallback=\"false\"> </span>"));

        return new ModuleData(submodulesText);
    }

    private String percent(double d) {
        return FormatPercent.format(d);
    }

    private Optional<String> percentColors(double d) {
        if (d < .5d) {
            return Optional.of("lime");
        } else if (d < .8d) {
            return Optional.of("yellow");
        } else {
            return Optional.of("red");
        }
    }

    private Optional<String> swapColors(double d) {
        if (d < .1d) {
            return Optional.of("lime");
        } else if (d < .4d) {
            return Optional.of("yellow");
        } else {
            return Optional.of("red");
        }
    }

    private String avgBytes(long bytes) {
        return FormatBytes.format((double) bytes / (double) SAMPLE_SIZE);
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
        return getter.applyAsLong(samples.get(samples.size() - 1)) - getter.applyAsLong(samples.get(
                0));
    }
}
