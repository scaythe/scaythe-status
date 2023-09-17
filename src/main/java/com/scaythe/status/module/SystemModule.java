package com.scaythe.status.module;

import static java.util.stream.Collectors.joining;

import com.scaythe.status.format.FormatBytes;
import com.scaythe.status.format.FormatPercent;
import com.scaythe.status.markup.PangoMarkup;
import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.module.sub.Submodule;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

public class SystemModule extends SamplingModule<SystemData> {

  private static final String MARKUP = PangoMarkup.NAME;

  private final SystemInfo systemInfo = new SystemInfo();
  private final AtomicReference<long[]> cpuTicks = new AtomicReference<>();

  private final List<Submodule<SystemData, ?>> submodules;

  public SystemModule(SamplingModuleConfig config, Consumer<ModuleData> output) {
    super(config, output);

    submodules =
        List.of(
            new Submodule<>("\uF2DB", SystemData::cpu, FormatPercent::format, this::percentColors),
            new Submodule<>(
                "\uF0C9", SystemData::memory, FormatPercent::format, this::percentColors),
            new Submodule<>("\uF0EC", SystemData::swap, FormatPercent::format, this::swapColors),
            new Submodule<>("\uF2DB", SystemData::disk, FormatPercent::format, this::percentColors),
            new Submodule<>("\uF019", SystemData::netDown, FormatBytes::format, d -> null),
            new Submodule<>("\uF093", SystemData::netUp, FormatBytes::format, d -> null));
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
    return new SystemData(cpu(), memory(), swap(), disk(), netDown(), netUp());
  }

  private double cpu() {
    long[] ticks =
        cpuTicks.getAndSet(systemInfo.getHardware().getProcessor().getSystemCpuLoadTicks());

    if (ticks == null) return 0;

    return systemInfo.getHardware().getProcessor().getSystemCpuLoadBetweenTicks(ticks);
  }

  private double memory() {
    long total = systemInfo.getHardware().getMemory().getTotal();
    long free = systemInfo.getHardware().getMemory().getAvailable();

    long used = total - free;

    return (double) used / (double) total;
  }

  private double swap() {
    long total = systemInfo.getHardware().getMemory().getVirtualMemory().getSwapTotal();
    long used = systemInfo.getHardware().getMemory().getVirtualMemory().getSwapUsed();

    return (double) used / (double) total;
  }

  private double disk() {
    return systemInfo.getOperatingSystem().getFileSystem().getFileStores().stream()
        .filter(s -> s.getMount().equals("/"))
        .findFirst()
        .map(this::disk)
        .orElse(0d);
  }

  private double disk(OSFileStore osFileStore) {
    long total = osFileStore.getTotalSpace();
    long used = total - osFileStore.getFreeSpace();

    return (double) used / (double) total;
  }

  private long netDown() {
    return systemInfo.getHardware().getNetworkIFs().stream()
        .mapToLong(NetworkIF::getBytesRecv)
        .sum();
  }

  private long netUp() {
    return systemInfo.getHardware().getNetworkIFs().stream()
        .mapToLong(NetworkIF::getBytesSent)
        .sum();
  }

  @Override
  public ModuleData reduce(List<SystemData> samples) {
    SystemData avg = reduceData(samples);

    String submodulesText =
        submodules.stream()
            .map(s -> s.format(avg))
            .collect(joining("<span fallback=\"false\"> </span>"));

    return ModuleData.ofMarkup(submodulesText, MARKUP, name());
  }

  private String percentColors(double d) {
    if (d < .5d) {
      return "lime";
    } else if (d < .8d) {
      return "yellow";
    } else {
      return "red";
    }
  }

  private String swapColors(double d) {
    if (d < .1d) {
      return "lime";
    } else if (d < .4d) {
      return "yellow";
    } else {
      return "red";
    }
  }

  private SystemData reduceData(List<SystemData> samples) {
    return new SystemData(
        average(samples, SystemData::cpu),
        average(samples, SystemData::memory),
        average(samples, SystemData::swap),
        last(samples, SystemData::disk),
        difference(samples, SystemData::netDown),
        difference(samples, SystemData::netUp));
  }

  private double last(List<SystemData> samples, ToDoubleFunction<SystemData> getter) {
    return getter.applyAsDouble(samples.get(samples.size() - 1));
  }

  private double average(List<SystemData> samples, ToDoubleFunction<SystemData> getter) {
    return samples.stream().mapToDouble(getter).average().orElse(0);
  }

  private long difference(List<SystemData> samples, ToLongFunction<SystemData> getter) {
    return getter.applyAsLong(samples.get(samples.size() - 1)) - getter.applyAsLong(samples.get(0));
  }
}
