package com.scaythe.status.module;

import org.immutables.value.Value;

@Value.Immutable
public interface SystemData {

    double cpu();

    double memory();

    double swap();

    long netDown();

    long netUp();
}
