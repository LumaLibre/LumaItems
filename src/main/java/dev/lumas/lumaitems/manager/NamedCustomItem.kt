package dev.lumas.lumaitems.manager

import dev.lumas.lumacore.utility.ContextLogger
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.registry.RegistryItem
import dev.lumas.lumaitems.registry.StringIdentifier
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class NamedCustomItem(
    val customItem: CustomItem,
    val forcedIdentifier: String? = null
) : RegistryItem {

    constructor(customItem: CustomItem) : this(customItem, null)

    private companion object {
        val LOGGER: ContextLogger = ContextLogger.getLogger(true)
    }

    override fun identifier(): StringIdentifier {
        return if (forcedIdentifier != null) {
            StringIdentifier.of(forcedIdentifier)
        } else {
            StringIdentifier.of(this.normalizedName())
        }
    }

    fun normalizedName(): String? {
        val itemStack = try {
            customItem.createItem().second
        } catch (e: Exception) {
            LOGGER.error("Failed to create item for " + customItem.javaClass.getSimpleName(), e)
            return null
        }
        val meta = itemStack.itemMeta
        if (!meta.hasCustomName()) {
            LOGGER.error("Item " + itemStack.type + " does not have a display name or meta!")
            return "${itemStack.type}-${randomString(3)}"
        }
        return PlainTextComponentSerializer.plainText().serialize(meta.customName()!!)
            .replace(" ", "_").lowercase()
    }

    private fun randomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}