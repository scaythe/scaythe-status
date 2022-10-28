package com.scaythe.status.module;

import com.scaythe.status.format.FormatBytes;
import com.scaythe.status.format.FormatPercent;
import com.scaythe.status.markup.PangoMarkup;
import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.module.sub.Submodule;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

public class SystemModule extends SamplingModule<SystemData> {

  private static final String MARKUP = PangoMarkup.NAME;

  private final SystemInfo systemInfo = new SystemInfo();
  private final AtomicReference<long[]> cpuTicks = new AtomicReference<>();

  private final List<Submodule<SystemData, ?>> submodules;

  public SystemModule(SamplingModuleConfig config) {
    super(config);

    submodules =
        List.of(
            new Submodule<>("\uF2DB", SystemData::cpu, FormatPercent::format, this::percentColors),
            new Submodule<>(
                "\uF0C9", SystemData::memory, FormatPercent::format, this::percentColors),
            new Submodule<>("\uF0EC", SystemData::swap, FormatPercent::format, this::swapColors),
            new Submodule<>(
                "\uF2DB", SystemData::disks, FormatPercent::format, this::percentColors),
            new Submodule<>(
                "\uF019", SystemData::netDown, FormatBytes::format, d -> Optional.<String>empty()),
            new Submodule<>(
                "\uF093", SystemData::netUp, FormatBytes::format, d -> Optional.<String>empty()));
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
    return SystemData.builder()
        .cpu(cpu())
        .memory(memory())
        .swap(swap())
        .disks(disks())
        .netDown(netDown())
        .netUp(netUp())
        .build();
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

  private List<Double> disks() {
    return systemInfo.getOperatingSystem().getFileSystem().getFileStores().stream()
        .map(this::disk)
        .toList();
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
            .collect(Collectors.joining("<span fallback=\"false\"> </span>"));

    return ModuleData.ofMarkup(submodulesText, MARKUP, name());
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

  private SystemData reduceData(List<SystemData> samples) {
    return SystemData.builder()
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
