package dev.lumas.lumaitems.util;

import dev.lumas.lumaitems.enums.Action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FireAnyways {
    Action[] value();
}
