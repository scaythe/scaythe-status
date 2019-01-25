package com.scaythe.status.write;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import jdk.internal.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

@Component
public class StatusWriter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final Writer writer;
    private final JsonWriter jsonWriter;

    public StatusWriter(Gson gson) throws IOException {
        this.gson = gson;
        this.writer = new OutputStreamWriter(System.out);
        this.jsonWriter = gson.newJsonWriter(this.writer);
        this.jsonWriter.setLenient(true);

        StatusHeader header = StatusHeaderImmutable.builder().version(1).clickEvents(true).build();

        write(header);

        jsonWriter.beginArray();
        writer.append('\n');
    }

    private void write(StatusHeader header) throws IOException {
        gson.toJson(header, StatusHeader.class, jsonWriter);
        writer.append('\n');
        jsonWriter.flush();
    }

    public void write(List<ModuleData> data) {
        Type t = new TypeToken<List<ModuleData>>() {}.getType();

        try {
            gson.toJson(data, t, jsonWriter);
            writer.append('\n');
            jsonWriter.flush();
        } catch (IOException e) {
            log.error("problem writing to std out : {} : {}", e.getClass(), e.getMessage());
            log.error("", e);
        }
    }
}
