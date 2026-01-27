package dev.lumas.lumaitems.configuration.model

class AstralSetClass<T>(
    val setClass: Class<T>
) {
    companion object {
        fun <T> of(setClass: Class<T>): AstralSetClass<T> {
            return AstralSetClass(setClass)
        }
    }
}