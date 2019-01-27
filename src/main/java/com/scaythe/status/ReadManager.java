package com.scaythe.status;

import com.scaythe.status.input.ClickEvent;
import com.scaythe.status.input.EventReader;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.util.Objects;

@Component
public class ReadManager implements SmartLifecycle {

    private final ModuleManager moduleManager;
    private final EventReader reader;

    private Disposable disposable = null;

    public ReadManager(ModuleManager moduleManager, EventReader reader) {
        this.moduleManager = moduleManager;
        this.reader = reader;
    }

    @Override
    public void start() {
        disposable = reader.events().doOnNext(this::event).subscribe();
    }

    @Override
    public void stop() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    public boolean isRunning() {
        return disposable != null;
    }

    private void event(ClickEvent event) {
        moduleManager.modules()
                .stream()
                .filter(m -> m.name().equals(event.name().orElse(null)))
                .filter(m -> Objects.equals(m.instance(), event.instance()))
                .forEach(m -> m.event(event));
    }
}
