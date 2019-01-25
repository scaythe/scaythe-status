package com.scaythe.status.write;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatusWriter {

    private final Gson json;

    public StatusWriter(Gson json) {
        this.json = json;
    }

    public void write(StatusHeader header) {
        json.toJson(header, System.out);
        System.out.println();
        System.out.println("[");
    }

    public void write(List<ModuleData> data) {
        json.toJson(data, System.out);
        System.out.println(",");
    }
}
