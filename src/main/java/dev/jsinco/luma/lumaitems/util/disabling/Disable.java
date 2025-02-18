package dev.jsinco.luma.lumaitems.util.disabling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE) // maybe add method support
@Retention(RetentionPolicy.RUNTIME)
public @interface Disable {
    WorldName[] value();
    boolean hard() default false;
}
