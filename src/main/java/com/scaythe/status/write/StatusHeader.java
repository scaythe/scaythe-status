package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

public record StatusHeader(
    int version,
    @SerializedName("stop_signal") @Nullable Integer stopSignal,
    @SerializedName("cont_signal") @Nullable Integer contSignal,
    @SerializedName("click_events") @Nullable Boolean clickEvents) {}
