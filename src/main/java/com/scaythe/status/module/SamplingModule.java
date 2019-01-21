package com.scaythe.status.module;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public abstract class SamplingModule<T> extends StatusModule {

    private final Duration sampleRate;
    private final Queue<T> samples;

    private Disposable disposable = null;

    public SamplingModule(Duration sampleRate, int size, String name, String instance, Runnable update) {
        super(name, instance, update);

        this.samples = new LimitedQueue<>(size);
        this.sampleRate = sampleRate;
    }

    @Override
    public void start() {
        if (disposable != null) {
            return;
        }

        disposable = Flux.interval(sampleRate)
                .map(l -> sample())
                .doOnNext(samples::add)
                .map(s -> reduce())
                .doOnNext(this::update)
                .subscribe();
    }

    @Override
    public void stop() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public boolean isRunning() {
        return disposable != null;
    }

    public abstract T sample();

    private ModuleData reduce() {
        return reduce(new ArrayList<>(samples));
    }

    public abstract ModuleData reduce(List<T> samples);
}
