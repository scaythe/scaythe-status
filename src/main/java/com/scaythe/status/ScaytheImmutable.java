package com.scaythe.status;


import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Value.Style(stagedBuilder = true,
        typeImmutable = "*Immutable",
        typeModifiable = "*Modifiable",
        get = {},
        depluralize = true,
        depluralizeDictionary = {})
public @interface ScaytheImmutable {}
