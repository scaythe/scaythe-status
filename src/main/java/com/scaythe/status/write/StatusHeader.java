package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@Gson.TypeAdapters
public interface StatusHeader {

    int version();

    @SerializedName("stop_signal")
    Optional<Integer> stopSignal();

    @SerializedName("cont_signal")
    Optional<Integer> contSignal();

    @SerializedName("click_events")
    Optional<Boolean> clickEvents();
}
