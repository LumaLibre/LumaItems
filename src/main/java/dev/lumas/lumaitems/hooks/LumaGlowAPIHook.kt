package dev.lumas.lumaitems.hooks

import dev.lumas.lumaitems.registry.Identifier
import dev.lumas.lumaitems.registry.StringIdentifier

class LumaGlowAPIHook : Hook {
    override fun identifier(): Identifier {
        return StringIdentifier.of("LumaGlowAPI")
    }
}