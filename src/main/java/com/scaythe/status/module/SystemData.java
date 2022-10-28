package com.scaythe.status.module;

import java.util.List;
import lombok.Builder;

@Builder
public record SystemData(
    double cpu, double memory, double swap, List<Double> disks, long netDown, long netUp) {}
