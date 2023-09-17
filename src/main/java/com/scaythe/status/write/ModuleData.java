package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;
import org.jspecify.annotations.Nullable;

public record ModuleData(
    @SerializedName("full_text") String fullText,
    @SerializedName("short_text") @Nullable String shortText,
    @Nullable String color,
    @Nullable String background,
    @Nullable String border,
    @SerializedName("min_width") @Nullable String minWidth,
    @Nullable String align,
    @Nullable String name,
    @Nullable String instance,
    @Nullable Boolean urgent,
    @Nullable Boolean separator,
    @SerializedName("separator_block_width") @Nullable String separatorBlockWidth,
    @Nullable String markup) {

  public static ModuleData of(String fullText, String name) {
    return new ModuleData(
        fullText, null, null, null, null, null, null, name, null, null, null, null, null);
  }

  public static ModuleData ofColor(String fullText, String color, String name) {
    return new ModuleData(
        fullText, null, color, null, null, null, null, name, null, null, null, null, null);
  }

  public static ModuleData ofMarkup(String fullText, String markup, String name) {
    return new ModuleData(
        fullText, null, null, null, null, null, null, name, null, null, null, null, markup);
  }

  public static ModuleData empty(String name) {
    return of("", name);
  }
}
