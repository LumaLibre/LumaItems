package dev.lumas.lumaitems.commands.providers

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import dev.lumas.core.model.brigadier.ArgumentTypeProvider
import io.papermc.paper.command.brigadier.argument.CustomArgumentType

class FreeFormStringProvider : ArgumentTypeProvider {
    override fun provide() = FreeFormStringArgumentType()

    class FreeFormStringArgumentType : CustomArgumentType<String, String> {
        companion object {
            private val NATIVE = StringArgumentType.word()
        }

        override fun parse(reader: StringReader): String {
            val start = reader.cursor
            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip()
            }
            return reader.string.substring(start, reader.cursor)
        }

        override fun getNativeType(): ArgumentType<String> = NATIVE
    }
}