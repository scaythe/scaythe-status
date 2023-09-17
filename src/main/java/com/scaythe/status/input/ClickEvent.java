package com.scaythe.status.input;

import com.google.gson.annotations.SerializedName;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record ClickEvent(
    @Nullable String name,
    @Nullable String instance,
    int button,
    int x,
    int y,
    @SerializedName("relative_x") int relativeX,
    @SerializedName("relative_y") int relativeY,
    int width,
    int height,
    Set<String> modifiers) {}
