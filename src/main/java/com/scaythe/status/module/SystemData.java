package com.scaythe.status.module;

public class SystemData {

    private final double cpu;
    private final double memory;
    private final double swap;
    private final long netDown;
    private final long netUp;

    public SystemData(double cpu, double memory, double swap, long netDown, long netUp) {
        this.cpu = cpu;
        this.memory = memory;
        this.swap = swap;
        this.netUp = netUp;
        this.netDown = netDown;
    }

    public double cpu() {
        return cpu;
    }

    public double memory() {
        return memory;
    }

    public double swap() {
        return swap;
    }

    public long netDown() {
        return netDown;
    }

    public long netUp() {
        return netUp;
    }
}
