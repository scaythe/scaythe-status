package com.scaythe.status.write;

import com.google.gson.Gson;
import com.scaythe.status.module.StatusModule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StatusWriter {
    private final Gson json = new Gson();

    public void write(List<StatusModule> modules) {
        System.out.println(json.toJson(modules.stream().map(this::module).collect(Collectors.toList())) + ",");
    }

    private ModuleJson module(StatusModule module) {
        return new ModuleJson(module.data().text(),
                module.data().color().orElse(null),
                module.name(),
                module.instance().orElse(null),
                module.markup().orElse(null));
    }
}
