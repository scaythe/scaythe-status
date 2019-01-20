package com.scaythe.status.module;

import java.util.Objects;
import java.util.Optional;

public class ModuleData {

    private final String text;
    private final String color;

    public ModuleData(String text) {
        this(text, null);
    }

    public ModuleData(String text, String color) {
        this.text = text;
        this.color = color;
    }

    public String text() {
        return text;
    }

    public Optional<String> color() {
        return Optional.ofNullable(color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleData that = (ModuleData) o;

        if (!text.equals(that.text)) return false;
        return Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + (color != null ? color.hashCode() : 0);
        return result;
    }
}
