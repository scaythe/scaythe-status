package com.scaythe.status.module.config;

import java.time.Duration;
import java.util.Optional;

public interface SamplingModuleConfigTemplate extends ModuleConfigTemplate {

    String SAMPLE_RATE = "sample-rate";
    String SIZE = "size";

    default Optional<Duration> sampleRate() {
        return Optional.ofNullable((Duration) properties().get(SAMPLE_RATE));
    }

    default Optional<Integer> size() {
        return Optional.ofNullable((Integer) properties().get(SIZE));
    }
}
