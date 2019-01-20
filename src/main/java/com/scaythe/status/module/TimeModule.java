package com.scaythe.status.module;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeModule extends StatusModule {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Disposable disposable = null;

    public TimeModule(Runnable update) {
        super(update);
    }

    @Override
    public void run() {
        update(date());

        if (disposable != null) {
            return;
        }

        disposable = Flux.interval(Duration.ofMillis(100)).map(l -> date()).doOnNext(this::update).subscribe();
    }

    private ModuleData date() {
        return new ModuleData(formatter.format(Instant.now().atZone(ZoneId.systemDefault())));
    }

    @Override
    public void stop() {
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
