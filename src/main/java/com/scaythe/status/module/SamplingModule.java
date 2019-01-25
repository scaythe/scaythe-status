package com.scaythe.status.module;

import com.scaythe.status.module.config.SamplingModuleConfigTemplate;
import com.scaythe.status.write.ModuleData;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SamplingModule<T> extends Module {

    private final Duration sampleRate;
    private final int size;

    public SamplingModule(SamplingModuleConfigTemplate config) {
        super(config);

        this.sampleRate = config.sampleRate().orElseGet(this::defaultSampleRate);
        this.size = config.size().orElseGet(this::defaultSize);
    }

    @Override
    public Flux<ModuleData> data() {
        Collection<T> samples = new LimitedQueue<>(size);

        return Flux.interval(sampleRate)
                .map(l -> sample())
                .doOnNext(samples::add)
                .map(s -> reduce(new ArrayList<>(samples)));
    }

    public Duration sampleRate() {
        return sampleRate;
    }

    public int size() {
        return size;
    }

    public abstract Duration defaultSampleRate();

    public abstract int defaultSize();

    public abstract T sample();

    public abstract ModuleData reduce(List<T> samples);
}
