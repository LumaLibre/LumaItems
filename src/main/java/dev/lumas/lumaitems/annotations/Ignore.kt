package dev.lumas.lumaitems.annotations

/**
 * An annotation describing that an item should not be reflected on and should
 * be ignored when registering items.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore 
