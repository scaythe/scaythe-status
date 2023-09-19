package com.scaythe.status.input;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventReader {
  private final Gson json;

  public void read(Consumer<ClickEvent> consumer) {
    try (JsonReader reader = json.newJsonReader(new InputStreamReader(System.in))) {
      reader.beginArray();

      while (!Thread.interrupted() && reader.hasNext()) {
        ClickEvent clickEvent = readEvent(reader);
        if (clickEvent != null) consumer.accept(clickEvent);
      }
    } catch (IOException e) {
      log.atError()
          .setCause(e)
          .log("problem reading json input : {} : {}", e.getClass().getName(), e.getMessage());
    }
  }

  private @Nullable ClickEvent readEvent(JsonReader reader) {
    try {
      return json.fromJson(reader, ClickEvent.class);
    } catch (JsonIOException e) {
      log.atError()
          .setCause(e)
          .log("problem reading json input : {} : {}", e.getClass().getName(), e.getMessage());
    } catch (JsonSyntaxException e) {
      log.atError()
          .setCause(e)
          .log("malformed json input : {} : {}", e.getClass().getName(), e.getMessage());
    }

    return null;
  }
}
