package dev.lumas.lumaitems.commands.providers

import com.mojang.brigadier.arguments.IntegerArgumentType
import dev.lumas.core.model.brigadier.ArgumentTypeProvider

class NonNegativeIntProvider : ArgumentTypeProvider {
    override fun provide(): IntegerArgumentType = IntegerArgumentType.integer(0)
}