package dev.lumas.lumaitems.enums

enum class TriState {
    TRUE,
    FALSE,
    NOT_SET;

    fun toBoolean(default: Boolean = false): Boolean {
        return when (this) {
            TRUE -> true
            FALSE -> false
            NOT_SET -> default
        }
    }

    fun not() : TriState {
        return when (this) {
            TRUE -> FALSE
            FALSE -> TRUE
            NOT_SET -> NOT_SET
        }
    }

    companion object {
        fun fromBoolean(value: Boolean): TriState {
            return if (value) TRUE else FALSE
        }
    }
}