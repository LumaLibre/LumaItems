package dev.lumas.lumaitems.configuration.serdes

import dev.lumas.lumaitems.configuration.model.AstralSetClass
import dev.lumas.lumaitems.items.astral.AstralSet
import eu.okaeri.configs.schema.GenericsPair
import eu.okaeri.configs.serdes.BidirectionalTransformer
import eu.okaeri.configs.serdes.SerdesContext

class AstralSetClassTransformer : BidirectionalTransformer<String, AstralSetClass>() {
    override fun getPair(): GenericsPair<String, AstralSetClass> {
        return this.genericsPair(String::class.java, AstralSetClass::class.java)
    }

    //TODO
    override fun leftToRight(data: String, serdesContext: SerdesContext): AstralSetClass {
        val className = "dev.lumas.lumaitems.items.astral.sets.$data"
        val clazz = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Astral set class $className not found", e)
        }
        return AstralSetClass(clazz)
    }

    override fun rightToLeft(data: AstralSetClass, serdesContext: SerdesContext): String {
        return data.setClass.simpleName
    }
}