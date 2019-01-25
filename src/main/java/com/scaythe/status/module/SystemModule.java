package com.scaythe.status.module;

import com.scaythe.status.format.FormatBytes;
import com.scaythe.status.format.FormatPercent;
import com.scaythe.status.markup.PangoMarkup;
import com.scaythe.status.module.config.SamplingModuleConfigTemplate;
import com.scaythe.status.module.sub.Submodule;
import com.scaythe.status.write.ModuleData;
import com.scaythe.status.write.ModuleDataImmutable;
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

    private static final String MARKUP = PangoMarkup.NAME;

    private final SystemInfo systemInfo = new SystemInfo();

    private final List<Submodule<SystemData, ?>> submodules = new ArrayList<>();

    public SystemModule(SamplingModuleConfigTemplate config) {
        super(config);

        submodules.add(new Submodule<>("\uF2DB", SystemData::cpu, this::percent, this::percentColors));
        submodules.add(new Submodule<>("\uF0C9", SystemData::memory, this::percent, this::percentColors));
        submodules.add(new Submodule<>("\uF0EC", SystemData::swap, this::percent, this::swapColors));
        submodules.add(new Submodule<>("\uF019", SystemData::netDown, this::avgBytes, d -> Optional.empty()));
        submodules.add(new Submodule<>("\uF093", SystemData::netUp, this::avgBytes, d -> Optional.empty()));
    }

    @Override
    public String defaultName() {
        return "system";
    }

    @Override
    public Duration defaultSampleRate() {
        return Duration.ofSeconds(1);
    }

    @Override
    public int defaultSize() {
        return 10;
    }

    @Override
    public SystemData sample() {
        return SystemDataImmutable.builder()
                .cpu(cpu())
                .memory(memory())
                .swap(swap())
                .netDown(netDown())
                .netUp(netUp())
                .build();
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

        String submodulesText = submodules.stream()
                .map(s -> s.format(avg))
                .collect(Collectors.joining("<span fallback=\"false\"> </span>"));

        return ModuleDataImmutable.builder().fullText(submodulesText).name(name()).markup(MARKUP).build();
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
        return FormatBytes.format((double) bytes / (double) size());
    }

    private SystemData reduceData(List<SystemData> samples) {
        return SystemDataImmutable.builder()
                .cpu(average(samples, SystemData::cpu))
                .memory(average(samples, SystemData::memory))
                .swap(average(samples, SystemData::swap))
                .netDown(difference(samples, SystemData::netDown))
                .netUp(difference(samples, SystemData::netUp))
                .build();
    }

    private double average(List<SystemData> samples, ToDoubleFunction<SystemData> getter) {
        return samples.stream().mapToDouble(getter).average().orElse(0);
    }

    private long difference(List<SystemData> samples, ToLongFunction<SystemData> getter) {
        return getter.applyAsLong(samples.get(samples.size() - 1)) - getter.applyAsLong(samples.get(0));
    }
}
