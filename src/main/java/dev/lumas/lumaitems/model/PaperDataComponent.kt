@file:Suppress("UnstableApiUsage")
package dev.lumas.lumaitems.model

import io.papermc.paper.datacomponent.DataComponentType

abstract class PaperDataComponent {
    companion object {
        @JvmStatic
        fun <T : Any> valued(dataComponentType: DataComponentType.Valued<T>, value: T): ValuedPaperDataComponent<T> {
            return ValuedPaperDataComponent(dataComponentType, value)
        }

        fun <T: Any> valued(dataComponentType: DataComponentType.Valued<T>): ValuedPaperDataComponent.Builder<T> {
            return ValuedPaperDataComponent.Builder(dataComponentType)
        }

        @JvmStatic
        fun unValued(dataComponentType: DataComponentType.NonValued): UnValuedPaperDataComponent {
            return UnValuedPaperDataComponent(dataComponentType)
        }
    }
}

class UnValuedPaperDataComponent(
    val dataComponentType: DataComponentType.NonValued
) : PaperDataComponent()

class ValuedPaperDataComponent<T : Any>(
    val dataComponentType: DataComponentType.Valued<T>,
    val value: T,
) : PaperDataComponent() {

    class Builder<T : Any>(
        private val dataComponentType: DataComponentType.Valued<T>
    ) {

        fun value(value: T): ValuedPaperDataComponent<T> {
            return ValuedPaperDataComponent(dataComponentType, value)
        }
    }
}