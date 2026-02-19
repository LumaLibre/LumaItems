package dev.lumas.lumaitems.annotations

/**
 * A source annotation describing that a listener will check for an item in all equipment slots.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class AllSlots
