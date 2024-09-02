package com.scaythe.status.module;

import static java.util.stream.Collectors.joining;

import com.scaythe.status.format.FormatBytes;
import com.scaythe.status.format.FormatPercent;
import com.scaythe.status.markup.PangoMarkup;
import com.scaythe.status.module.config.SamplingModuleConfig;
import com.scaythe.status.module.sub.Submodule;
import com.scaythe.status.write.ModuleData;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.SequencedCollection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

@Slf4j
public class SystemModule extends SamplingModule<SystemData> {

  private static final String MARKUP = PangoMarkup.NAME;

  private final SystemInfo systemInfo = new SystemInfo();
  private final AtomicReference<long[]> cpuTicks = new AtomicReference<>();

  private final List<Submodule<SystemData, ?>> submodules;

  public SystemModule(SamplingModuleConfig config, Consumer<ModuleData> output) {
    super(config, output);

    submodules =
        List.of(
            new Submodule<>("\uF2DB", SystemData::cpu, FormatPercent::format, this::cpuColors),
            new Submodule<>("\uF538", SystemData::memory, FormatPercent::format, this::ramColors),
            new Submodule<>("\uF074", SystemData::swap, FormatPercent::format, this::swapColors),
            new Submodule<>("\uF0A0", SystemData::disk, FormatPercent::format, this::diskColors),
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
    log.atDebug().log("sampling");

    NetData netData = netData();

    return new SystemData(
        cpu(), memory(), swap(), disk(), netData.label(), netData.down(), netData.up());
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

  private record NetData(String label, long down, long up) {}

  private NetData netData() {
    return systemInfo.getHardware().getNetworkIFs().stream()
        .filter(n -> n.getName().equals("eno1"))
        .map(SystemModule::getNetData)
        .findFirst()
        .orElse(new NetData("", 0, 0));
  }

  private static NetData getNetData(NetworkIF n) {
    String ip = Arrays.stream(n.getIPv4addr()).findFirst().orElse("");
    String label = "%s:%s".formatted(n.getName(), ip);
    return new NetData(label, n.getBytesRecv(), n.getBytesSent());
  }

  @Override
  public ModuleData reduce(SequencedCollection<SystemData> samples) {
    SystemData avg = reduceData(samples);

    String submodulesText =
        submodules.stream()
            .map(s -> s.format(avg))
            .collect(joining("<span fallback=\"false\"> </span>"));

    return ModuleData.ofMarkup(submodulesText, MARKUP, name());
  }

  private String cpuColors(double d) {
    return colors(d, .5d, .8d);
  }

  private String ramColors(double d) {
    return colors(d, .5d, .8d);
  }

  private String diskColors(double d) {
    return colors(d, .8d, .95d);
  }

  private String swapColors(double d) {
    return colors(d, .2d, .5d);
  }

  private String colors(double d, double warnThreshold, double errorThreshold) {
    if (d > errorThreshold) return "red";
    if (d > warnThreshold) return "yellow";
    return "lime";
  }

  private SystemData reduceData(SequencedCollection<SystemData> samples) {
    return new SystemData(
        average(samples, SystemData::cpu),
        average(samples, SystemData::memory),
        average(samples, SystemData::swap),
        last(samples, SystemData::disk),
        last(samples, SystemData::netInterface),
        difference(samples, SystemData::netDown),
        difference(samples, SystemData::netUp));
  }

  private <T> T last(SequencedCollection<SystemData> samples, Function<SystemData, T> getter) {
    return getter.apply(samples.getLast());
  }

  private double average(
      SequencedCollection<SystemData> samples, ToDoubleFunction<SystemData> getter) {
    return samples.stream().mapToDouble(getter).average().orElse(0);
  }

  private long difference(
      SequencedCollection<SystemData> samples, ToLongFunction<SystemData> getter) {
    return getter.applyAsLong(samples.getLast()) - getter.applyAsLong(samples.getFirst());
  }
}
