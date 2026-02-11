package dev.lumas.lumaitems.annotations

import dev.lumas.lumaitems.enums.Action

/**
 * An annotation describing that a specific action should be executed for an item
 * even if the item is not being held or would not be detected normally.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FireAnyways(vararg val value: Action)
