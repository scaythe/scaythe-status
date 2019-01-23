package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;

public class ModuleJson {

    @SerializedName("full_text")
    private final String fullText;
    private final String color;
    private final String name;
    private final String instance;
    private final String markup;

    public ModuleJson(
            String fullText, String color, String name, String instance, String markup) {
        this.fullText = fullText;
        this.color = color;
        this.name = name;
        this.instance = instance;
        this.markup = markup;
    }

    public String fullText() {
        return fullText;
    }

    public String color() {
        return color;
    }

    public String name() {
        return name;
    }

    public String instance() {
        return instance;
    }

    public String markup() {
        return markup;
    }
}
