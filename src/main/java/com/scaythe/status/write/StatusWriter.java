package com.scaythe.status.write;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import lombok.extern.flogger.Flogger;
import org.springframework.stereotype.Component;

@Component
@Flogger
public class StatusWriter {

  private final Gson gson;
  private final Writer writer;
  private final JsonWriter jsonWriter;

  public StatusWriter(Gson gson) throws IOException {
    this.gson = gson;
    writer = new OutputStreamWriter(System.out);
    jsonWriter = gson.newJsonWriter(writer);
  }

  public void writeHeader() {
    StatusHeader header = new StatusHeader(1, null, null, true);

    writeHeader(header, gson, writer);

    try {
      jsonWriter.beginArray();
      writer.append('\n');
      jsonWriter.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeHeader(StatusHeader header, Gson gson, Writer writer) {
    try {
      gson.toJson(header, writer);
      writer.append('\n');
      writer.flush();
    } catch (IOException e) {
      log.atSevere().withCause(e).log(
          "problem writing header to std out : %s : %s", e.getClass(), e.getMessage());
    }
  }

  public void write(List<ModuleData> data) {
    Type t = new TypeToken<List<ModuleData>>() {}.getType();

    try {
      gson.toJson(data, t, jsonWriter);
      writer.append('\n');
      jsonWriter.flush();
    } catch (IOException e) {
      log.atSevere().withCause(e).log(
          "problem writing data to std out : %s : %s", e.getClass(), e.getMessage());
    }
  }
}
