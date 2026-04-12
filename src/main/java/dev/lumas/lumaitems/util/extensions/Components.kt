@file:JvmName("Components")
package dev.lumas.lumaitems.util.extensions

import dev.lumas.core.util.Text
import dev.lumas.lumaitems.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun Component.asPlainText(): String {
    return PlainTextComponentSerializer.plainText().serialize(this)
}

fun String.asComponent(italic: Boolean = false): Component {
    return if (italic) {
        Text.mm(this)
    } else {
        Text.mmNoItalic(this)
    }
}

fun List<String>.asComponent(italic: Boolean = false): List<Component> {
    return if (italic) {
        Text.mml(this)
    } else {
        Text.mmlNoItalic(this)
    }
}

fun String.legacy(): String {
    val colorCoded = Util.colorcode(this)
    val legacyPart: Component = LegacyComponentSerializer.legacySection().deserialize(colorCoded)
    return MiniMessage.miniMessage().serialize(legacyPart)
}

fun List<String>.legacy(): List<String> {
    return this.map { it.legacy() }
}

fun String.colorcode(legacy: Boolean): Component {
    return if (legacy) {
        this.legacy().asComponent()
    } else {
        this.asComponent()
    }
}

fun List<String>.colorcode(legacy: Boolean): List<Component> {
    return this.map { it.colorcode(legacy) }
}