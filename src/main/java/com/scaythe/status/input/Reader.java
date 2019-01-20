package com.scaythe.status.input;

import com.scaythe.status.ModuleManager;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Reader implements SmartLifecycle {
    private static final Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"(.+?)\"");
    private static final Pattern instancePattern = Pattern.compile("\"instance\"\\s*:\\s*\"(.+?)\"");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ModuleManager moduleManager;

    public Reader(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public void start() {
        executor.execute(this::read);
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private void read() {
        try (Scanner scan = new Scanner(System.in)) {
            while (!Thread.interrupted() && scan.hasNextLine()) {
                String line = scan.nextLine();

                match(line).ifPresent(moduleManager::event);
            }
        }
    }

    private Optional<ClickEvent> match(String line) {
        Optional<String> name = match(line, namePattern);
        Optional<String> instance = match(line, instancePattern);

        return name.map(n -> new ClickEvent(n, instance.orElse(null)));
    }

    private Optional<String> match(String line, Pattern p) {
        Matcher m = p.matcher(line);

        if (m.find()) {
            return Optional.of(m.group(1));
        }

        return Optional.empty();
    }
}
