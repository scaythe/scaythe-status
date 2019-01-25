package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@Gson.TypeAdapters
public interface ModuleData {

    @SerializedName("full_text")
    String fullText();

    @SerializedName("short_text")
    Optional<String> shortText();

    Optional<String> color();

    Optional<String> background();

    Optional<String> border();

    @SerializedName("min_width")
    Optional<String> minWidth();

    Optional<String> align();

    Optional<String> name();

    Optional<String> instance();

    Optional<Boolean> urgent();

    Optional<Boolean> separator();

    @SerializedName("separator_block_width")
    Optional<String> separatorBlockWidth();

    Optional<String> markup();
}
