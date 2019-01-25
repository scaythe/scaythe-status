package com.scaythe.status.input;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@Gson.TypeAdapters
public interface ClickEvent {
    Optional<String> name();

    Optional<String> instance();

    int button();

    int x();

    int y();

    @SerializedName("relative_x")
    int relativeX();

    @SerializedName("relative_y")
    int relativeY();

    int width();

    int height();

    Set<String> modifiers();
}
