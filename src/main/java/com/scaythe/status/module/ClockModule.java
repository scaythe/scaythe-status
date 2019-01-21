package com.scaythe.status.module;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClockModule extends SamplingModule<Instant> {

    private static final String NAME = "clock";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ClockModule(Runnable update) {
        this(null, update);
    }

    public ClockModule(String instance, Runnable update) {
        super(Duration.ofMillis(100), 1, NAME, instance, update);
    }

    @Override
    public Instant sample() {
        return Instant.now();
    }

    @Override
    public ModuleData reduce(List<Instant> samples) {
        return new ModuleData(formatter.format(samples.get(0).atZone(ZoneId.systemDefault())));
    }
}
