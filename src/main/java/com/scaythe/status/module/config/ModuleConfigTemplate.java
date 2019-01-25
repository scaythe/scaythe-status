package com.scaythe.status.module.config;

import java.util.Map;
import java.util.Optional;

public interface ModuleConfigTemplate {

    String NAME = "name";
    String INSTANCE = "instance";

    Map<String, Object> properties();

    default Optional<String> name() {
        return Optional.ofNullable((String) properties().get(NAME));
    }

    default Optional<String> instance() {
        return Optional.ofNullable((String) properties().get(INSTANCE));
    }
}
