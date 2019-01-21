package com.scaythe.status;

import com.scaythe.status.module.ModuleData;
import com.scaythe.status.module.StatusModule;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StatusWriter {

    public void write(List<StatusModule> modules) {
        System.out.println(modules(modules));
    }

    private String modules(List<StatusModule> modules) {
        return modules.stream().map(this::module).collect(Collectors.joining(",", "[", "],"));
    }

    private String module(StatusModule module) {
        Collection<String> data = new ArrayList<>();

        data.add(text(module.data()));
        color(module.data()).ifPresent(data::add);
        data.add(name(module));
        instance(module).ifPresent(data::add);
        markup(module).ifPresent(data::add);

        return data.stream().collect(Collectors.joining(",", "{", "}"));
    }

    private String text(ModuleData data) {
        return entry("full_text", data.text());
    }

    private Optional<String> color(ModuleData data) {
        return data.color().map(c -> entry("color", c));
    }

    private String name(StatusModule module) {
        return entry("name", module.name());
    }

    private Optional<String> instance(StatusModule module) {
        return module.instance().map(c -> entry("instance", c));
    }

    private Optional<String> markup(StatusModule module) {
        return module.markup().map(c -> entry("markup", c));
    }

    private String entry(String header, String value) {
        return MessageFormat.format("\"{0}\": \"{1}\"", header, value);
    }
}
