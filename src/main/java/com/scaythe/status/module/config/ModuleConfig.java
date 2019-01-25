package com.scaythe.status.module.config;

import org.immutables.value.Value;

@Value.Immutable
public interface ModuleConfig extends ModuleConfigTemplate {

    static ModuleConfig defaults() {
        return ModuleConfigImmutable.builder().build();
    }
}
