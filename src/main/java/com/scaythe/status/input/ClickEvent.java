package com.scaythe.status.input;

import java.util.Optional;

public class ClickEvent {

    private final String name;
    private final String instance;

    public ClickEvent(String name) {
        this(name, null);
    }

    public ClickEvent(String name, String instance) {
        this.name = name;
        this.instance = instance;
    }

    public String name() {
        return name;
    }

    public Optional<String> instance() {
        return Optional.ofNullable(instance);
    }
}
