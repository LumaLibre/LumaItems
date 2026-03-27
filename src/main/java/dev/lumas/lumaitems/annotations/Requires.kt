package dev.lumas.lumaitems.annotations

/**
 * An annotation describing that an item requires a specific external plugin
 * to be present on the server to be registered.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Requires(val value: String)
