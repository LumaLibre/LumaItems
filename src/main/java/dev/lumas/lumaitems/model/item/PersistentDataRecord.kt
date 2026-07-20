package dev.lumas.lumaitems.model.item

import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.namespacedKey
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

class PersistentDataRecord<P, C : Any>(
    val nameSpacedKey: NamespacedKey,
    val persistentDataType: PersistentDataType<P, C>,
    val value: C,
) {
    companion object {
        val PREVENT_NETHERITE_SMITHING_KEY = "prevent-smithing".namespacedKey()
        val PREVENT_NETHERITE_SMITHING = create(PREVENT_NETHERITE_SMITHING_KEY, PersistentDataType.SHORT, 1)
        val COLORABLE_KEY = "colorable".namespacedKey()
        val COLORABLE = create(COLORABLE_KEY, PersistentDataType.SHORT, 1)
        val MIXABLE_KEY = "mixable".namespacedKey()
        val MIXABLE = create(MIXABLE_KEY, PersistentDataType.SHORT, 1)

        @JvmStatic
        fun <P, C : Any> create(
            key: String,
            persistentDataType: PersistentDataType<P, C>,
            value: C,
        ): PersistentDataRecord<P, C> {
            return PersistentDataRecord(Util.namespacedKey(key), persistentDataType, value)
        }

        @JvmStatic
        fun <P, C : Any> create(
            nameSpacedKey: NamespacedKey,
            persistentDataType: PersistentDataType<P, C>,
            value: C,
        ): PersistentDataRecord<P, C> {
            return PersistentDataRecord(nameSpacedKey, persistentDataType, value)
        }

        @JvmStatic
        fun <P, C : Any> builder() = Builder<P, C>()
    }

    class Builder<P, C : Any> {
        private var nameSpacedKey: NamespacedKey? = null
        private var persistentDataType: PersistentDataType<P, C>? = null
        private var value: C? = null

        fun key(key: String) = apply { this.nameSpacedKey = Util.namespacedKey(key) }
        fun key(nameSpacedKey: NamespacedKey) = apply { this.nameSpacedKey = nameSpacedKey }
        fun type(persistentDataType: PersistentDataType<P, C>) = apply { this.persistentDataType = persistentDataType }
        fun value(value: C) = apply { this.value = value }

        fun build() = PersistentDataRecord(nameSpacedKey!!, persistentDataType!!, value!!)
    }
}