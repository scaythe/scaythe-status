package com.scaythe.status.module;

import java.util.Objects;

public abstract class StatusModule implements Runnable {
    private final Runnable update;
    private ModuleData data = new ModuleData("");

    public StatusModule(Runnable update) {
        this.update = update;
    }

    protected void update(ModuleData data) {
        if (!Objects.equals(this.data, data)) {
            this.data = data;
            update.run();
        }
    }

    public ModuleData data() {
        return data;
    }

    public abstract void stop();
}
