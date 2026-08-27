package dev.lumas.lumaitems.model.item

import dev.lumas.core.util.ContextLogger
import dev.lumas.lumaitems.registry.RegistryItem
import dev.lumas.lumaitems.registry.StringIdentifier
import java.util.concurrent.CopyOnWriteArrayList
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class NamedCustomItem(
    val customItem: CustomItem,
    val forcedIdentifier: String? = null
) : RegistryItem {

    constructor(customItem: CustomItem) : this(customItem, null)

    companion object {
        private val LOGGER: ContextLogger = ContextLogger.getLogger(true)
        private val BRIGADIER_WORD_ALLOWED = Regex("[^a-zA-Z0-9_\\-.+]")

        @JvmStatic
        fun alias(name: StringIdentifier, item: CustomItem): NamedCustomItem {
            return NamedCustomItem(item, "${name.key()}_${keyOf(item)}")
        }

        private fun keyOf(item: CustomItem): String {
            return brigadierSafe(item.identifier().asSimpleString().lowercase())
        }

        private fun brigadierSafe(input: String): String {
            return input.replace(BRIGADIER_WORD_ALLOWED, "")
                .replace(Regex("_+"), "_") // replace multiple underscores with one
                .trim('_') // strip leading underscores
        }

        private fun randomString(length: Int): String {
            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }

    private val variants = CopyOnWriteArrayList(listOf(customItem))

    private val resolvedIdentifier: StringIdentifier by lazy {
        StringIdentifier.of(forcedIdentifier ?: normalizedName() ?: keyOf(customItem))
    }

    override fun identifier(): StringIdentifier {
        return resolvedIdentifier
    }

    fun variants(): List<CustomItem> = variants

    fun addVariant(item: CustomItem): Boolean {
        if (variants.any { it === item }) return false
        variants.add(item)
        return true
    }

    fun randomVariant(): CustomItem {
        return if (variants.size == 1) customItem else variants.random()
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
        val raw = PlainTextComponentSerializer.plainText().serialize(meta.customName()!!)
        return brigadierSafe(raw.replace(" ", "_").lowercase())
    }
}
