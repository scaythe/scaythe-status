package com.scaythe.status.input;

import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import java.util.Set;

public record ClickEvent(
    Optional<String> name,
    Optional<String> instance,
    int button,
    int x,
    int y,
    @SerializedName("relative_x") int relativeX,
    @SerializedName("relative_y") int relativeY,
    int width,
    int height,
    Set<String> modifiers) {}
