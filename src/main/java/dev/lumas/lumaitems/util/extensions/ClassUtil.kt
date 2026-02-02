@file:JvmName("ClassUtil")
package dev.lumas.lumaitems.util.extensions

fun classExists(className: String): Boolean {
    return try {
        Class.forName(className)
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

fun String.asClassOrNull(): Class<*>? {
    return try {
        Class.forName(this)
    } catch (e: ClassNotFoundException) {
        null
    }
}

fun <T> String.asClassOfOrNull(clazz: Class<T>): Class<out T>? {
    return try {
        val foundClass = Class.forName(this)
        if (clazz.isAssignableFrom(foundClass)) {
            @Suppress("UNCHECKED_CAST")
            foundClass as Class<out T>
        } else {
            null
        }
    } catch (e: ClassNotFoundException) {
        null
    }
}

fun <E : Enum<E>> String.asEnum(enumClass: Class<E>): E? {
    return try {
        java.lang.Enum.valueOf(enumClass, this.uppercase())
    } catch (_: IllegalArgumentException) {
        null
    }
}


fun String.formatSnakeCase(): String {
    return lowercase()
        .split('_')
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

fun <E : Enum<E>> Enum<E>.formatEnumerator(): String {
    return this.name.formatSnakeCase()
}