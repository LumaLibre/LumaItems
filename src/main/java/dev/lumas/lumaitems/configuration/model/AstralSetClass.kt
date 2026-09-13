package dev.lumas.lumaitems.configuration.model

import dev.lumas.lumaitems.items.astral.AstralSet
import kotlin.reflect.KClass

class AstralSetClass(
    var setClass: Class<*>
) {

    constructor(setClass: KClass<*>) : this(setClass.java)

    @Suppress("UNCHECKED_CAST")
    fun getAstralSetClass(): Class<out AstralSet> {
        return this.setClass as Class<out AstralSet>
    }
}