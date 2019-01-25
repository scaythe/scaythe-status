package com.scaythe.status.module.config;

import org.immutables.value.Value;

@Value.Immutable
public interface SamplingModuleConfig extends SamplingModuleConfigTemplate {

    static SamplingModuleConfig defaults() {
        return SamplingModuleConfigImmutable.builder().build();
    }
}
