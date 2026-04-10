@file:JvmName("Components")
package dev.lumas.lumaitems.util.extensions

import dev.lumas.core.util.Text
import net.kyori.adventure.text.Component
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