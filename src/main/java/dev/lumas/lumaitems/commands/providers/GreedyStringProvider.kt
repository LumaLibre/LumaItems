package dev.lumas.lumaitems.commands.providers

import com.mojang.brigadier.arguments.StringArgumentType
import dev.lumas.core.model.brigadier.ArgumentTypeProvider

class GreedyStringProvider : ArgumentTypeProvider {
    override fun provide(): StringArgumentType = StringArgumentType.greedyString()
}