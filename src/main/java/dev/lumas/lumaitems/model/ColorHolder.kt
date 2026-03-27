package dev.lumas.lumaitems.model

import java.awt.Color

class ColorHolder {

    private var color: Color = Color.WHITE

    fun adjustRed(amount: Int): ColorHolder {
        color = Color((color.red + amount).coerceIn(0, 255), color.green, color.blue)
        return this
    }

    fun adjustGreen(amount: Int): ColorHolder {
        color = Color(color.red, (color.green + amount).coerceIn(0, 255), color.blue)
        return this
    }

    fun adjustBlue(amount: Int): ColorHolder {
        color = Color(color.red, color.green, (color.blue + amount).coerceIn(0, 255))
        return this
    }

    fun fromHex(hex: String): ColorHolder {
        color = Color.decode(hex)
        return this
    }

    fun getColor(): Color {
        return color
    }

    fun asHex(): String {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    }
}