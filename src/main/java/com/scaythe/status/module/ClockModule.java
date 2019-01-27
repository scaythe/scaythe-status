package com.scaythe.status.module;

import com.scaythe.status.module.config.SamplingModuleConfigTemplate;
import com.scaythe.status.write.ModuleData;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClockModule extends SamplingModule<Instant> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss");

    public ClockModule(SamplingModuleConfigTemplate config) {
        super(config);
    }

    @Override
    public String defaultName() {
        return "clock";
    }

    @Override
    public Duration defaultSampleRate() {
        return Duration.ofMillis(20);
    }

    @Override
    public int defaultSize() {
        return 1;
    }

    @Override
    public Instant sample() {
        return Instant.now();
    }

    @Override
    public ModuleData reduce(List<Instant> samples) {
        return ModuleData.of(format(samples.get(samples.size() - 1)), name());
    }

    private String format(Instant instant) {
        return formatter.format(instant.atZone(ZoneId.systemDefault()));
    }
}
