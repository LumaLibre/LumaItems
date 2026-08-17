package dev.lumas.lumaitems.annotations;

import dev.lumas.lumaitems.enums.WorldGroup;
import dev.lumas.lumaitems.enums.WorldKey;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Disable {
    WorldKey[] value() default {};
    WorldGroup[] groups() default {};
    boolean hard() default false;
    boolean invert() default false; // if true, the item is disabled in all worlds EXCEPT the ones specified
}
