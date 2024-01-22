package com.scaythe.status.module;

public record SystemData(
    double cpu,
    double memory,
    double swap,
    double disk,
    String netInterface,
    long netDown,
    long netUp) {}
