package dev.lumas.lumaitems.model

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
        val PREVENT_NETHERITE_SMITHING = PersistentDataRecord.create(PREVENT_NETHERITE_SMITHING_KEY, PersistentDataType.SHORT, 1)

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
    }
}