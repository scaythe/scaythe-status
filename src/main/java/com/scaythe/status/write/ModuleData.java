package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import lombok.Builder;

public record ModuleData(
    @SerializedName("full_text") String fullText,
    @SerializedName("short_text") Optional<String> shortText,
    Optional<String> color,
    Optional<String> background,
    Optional<String> border,
    @SerializedName("min_width") Optional<String> minWidth,
    Optional<String> align,
    Optional<String> name,
    Optional<String> instance,
    Optional<Boolean> urgent,
    Optional<Boolean> separator,
    @SerializedName("separator_block_width") Optional<String> separatorBlockWidth,
    Optional<String> markup) {

  @Builder
  public ModuleData {
    // TODO remove when no longer needed for intellij lombok plugin, move @Builder to top level
  }

  public static ModuleData of(String fullText, String name) {
    return ModuleData.builder().fullText(fullText).name(Optional.of(name)).build();
  }

  public static ModuleData ofColor(String fullText, String color, String name) {
    return ModuleData.builder()
        .fullText(fullText)
        .name(Optional.of(name))
        .color(Optional.of(color))
        .build();
  }

  public static ModuleData ofMarkup(String fullText, String markup, String name) {
    return ModuleData.builder()
        .fullText(fullText)
        .name(Optional.of(name))
        .markup(Optional.of(markup))
        .build();
  }

  public static ModuleData empty(String name) {
    return of("", name);
  }
}
