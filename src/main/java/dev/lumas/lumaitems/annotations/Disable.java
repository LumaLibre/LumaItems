package dev.lumas.lumaitems.annotations;

import dev.lumas.lumaitems.enums.WorldName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Disable {
    WorldName[] value();
    boolean hard() default false;
    boolean invert() default false; // if true, the item is disabled in all worlds EXCEPT the ones specified in value()
    boolean vanilla() default false; // if true, this will add 'world', 'world_nether' and 'world_the_end' to the list of disabled worlds
    boolean standard() default false; // if true, this will add the standard worlds to the list of disabled worlds
}
