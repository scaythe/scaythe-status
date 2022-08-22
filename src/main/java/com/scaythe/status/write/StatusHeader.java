package com.scaythe.status.write;

import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import lombok.Builder;

public record StatusHeader(
    int version,
    @SerializedName("stop_signal") Optional<Integer> stopSignal,
    @SerializedName("cont_signal") Optional<Integer> contSignal,
    @SerializedName("click_events") Optional<Boolean> clickEvents) {

  @Builder
  public StatusHeader {
    // TODO remove when no longer needed for intellij lombok plugin, move @Builder to top level
  }
}
