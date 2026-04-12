@file:JvmName("Lazy")
package dev.lumas.lumaitems.util.extensions

fun <T> lazyListOf(vararg elements: T): Lazy<List<T>> =
    lazy { elements.toList() }

fun <T> lazySetOf(vararg elements: T): Lazy<Set<T>> =
    lazy { elements.toSet() }

fun <K, V> lazyMapOf(vararg pairs: Pair<K, V>): Lazy<Map<K, V>> =
    lazy { mapOf(*pairs) }

inline fun <reified T> lazyArrayOf(vararg elements: T): Lazy<Array<T>> =
    lazy { arrayOf(*elements) }

fun <T> lazyList(initializer: () -> List<T>): Lazy<List<T>> =
    lazy(initializer)

fun <T> lazySet(initializer: () -> Set<T>): Lazy<Set<T>> =
    lazy(initializer)

fun <K, V> lazyMap(initializer: () -> Map<K, V>): Lazy<Map<K, V>> =
    lazy(initializer)

fun <T> safeLazy(initializer: () -> T): Lazy<T?> =
    lazy {
        try { initializer() } catch (_: Exception) { null }
    }