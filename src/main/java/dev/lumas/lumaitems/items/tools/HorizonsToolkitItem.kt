package dev.lumas.lumaitems.items.tools

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.ItemManager
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.model.Mixable
import dev.lumas.lumaitems.model.PersistentDataRecord
import dev.lumas.lumaitems.registry.NamespacedIdentifier
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.asPlainText
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.withMeta
import dev.lumas.lumaitems.util.tiers.Tier
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType

class HorizonsToolkitItem : CustomItem, Mixable {

    private companion object {
        private const val DEFAULT_NAME = "Horizon's Toolkit"
        private const val CUSTOM_ENCHANT = "<#DE5264>Mix"
        private val KEY = "horizons-toolkit".namespacedKey()
        private val CUSTOM_ENCHANT_COMPONENT = CUSTOM_ENCHANT.asComponent()

        private fun name(name: String, star: Boolean = false): String {
            return "<b><gradient:#DE5264:#E27589:#F1A767:#ECC075:#A7A8D9:#414F86>$name</gradient></b>" + if (star) " <!b><gold>★</gold></!b>" else ""
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name(name(DEFAULT_NAME))
            .customEnchants(CUSTOM_ENCHANT)
            .persistentData(KEY)
            .material(Material.NETHERITE_PICKAXE)
            .persistentDataRecords(PersistentDataRecord.MIXABLE)
            .tier(Tier.WONDERLAND_2026.alt())
            .lore(
                "A <#DE5264>multitool</#DE5264> with the",
                "malleable ability to",
                "be combined with other",
                "items to gain their",
                "abilities.",
                "",
                "<#DE5264>Mix</#DE5264> this tool with",
                "any other item to in-",
                "herit its abilities.",
                "",
                "<red>Mixing is irreversible."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 6,
                Enchantment.UNBREAKING to 6,
                Enchantment.SHARPNESS to 5,
                Enchantment.LOOTING to 4,
                Enchantment.MENDING to 1,
            )
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val item = player.inventory.itemInMainHand

        val currentHandle = item.getHandle(item.type)
        currentHandle?.executeActions(type, player, event)


        when (type) {
            Action.ENTITY_DAMAGE -> {
                val intendedMaterial = Material.NETHERITE_SWORD

                if (intendedMaterial != item.type) {
                    val newHandle = item.getHandle(intendedMaterial)
                    val newItem = item.swapTo(newHandle, intendedMaterial)

                    player.inventory.setItemInMainHand(newItem)
                }
            }

            Action.LEFT_CLICK -> {
                event as PlayerInteractEvent
                val block = event.clickedBlock ?: return false

                var highestDestroySpeed: Pair<Material, Float> = Pair(item.type, block.getDestroySpeed(item))
                for (material in Type.getItemStacksExcluding(item.type)) {
                    val destroySpeed = block.getDestroySpeed(material)
                    if (destroySpeed <= highestDestroySpeed.second) {
                        continue
                    }
                    highestDestroySpeed = Pair(material.type, destroySpeed)
                }

                val intendedMaterial = highestDestroySpeed.first
                if (intendedMaterial != item.type) {
                    val newHandle = item.getHandle(intendedMaterial)
                    val newItem = item.swapTo(newHandle, intendedMaterial)

                    player.inventory.setItemInMainHand(newItem)
                }
            }

            Action.SWAP_HAND -> {
                event as PlayerSwapHandItemsEvent
                event.isCancelled = true
                val newItem = getDefault(item, item.type, false)
                player.inventory.setItemInMainHand(newItem)
            }

            else -> {}
        }


        return true
    }


    override fun mix(player: Player, item: ItemStack, other: ItemStack): ItemStack? {
        val otherItemHandle = ItemManager.getCustomItem(other) ?: run {
            player.send("That's not a custom item!")
            return null
        }

        val otherItemType = Type.fromType(other.type) ?: run {
            player.send("This tool doesn't support that material.")
            return null
        }

        val valueFromKey = item.getPersistentKey(otherItemType.key, PersistentDataType.STRING)

        if (valueFromKey != null) {
            player.send("This tool already has that material mixed!")
            return null
        }

        val mixed = item.doMix(otherItemHandle)
        return mixed.cloneDurability(item)
    }



    private fun ItemStack.swapTo(handle: CustomItem?, intendedMaterial: Material): ItemStack {
        if (handle == null) {
            return getDefault(this, intendedMaterial, true).cloneDurability(this)
        }

        val created = handle.createItem()
        val handleKey = created.first.namespacedKey()
        val newItem = created.second
        newItem.editPersistentDataContainer {
            this.persistentDataContainer.copyTo(it, true)
            it.remove(handleKey)
        }
        // copy durability
        return newItem.cloneDurability(this).setDecals()
    }

    private fun ItemStack.doMix(handle: CustomItem): ItemStack {
        val created = handle.createItem()
        val handleKey = created.first.namespacedKey()
        val newItem = created.second
        val toolType = Type.fromType(newItem.type) ?: return newItem
        this.setPersistentKey(toolType.key, PersistentDataType.STRING, handleKey.key)


        newItem.editPersistentDataContainer {
            this.persistentDataContainer.copyTo(it, false)
            it.remove(handleKey)
        }
        return newItem.setDecals()
    }

    private fun getDefault(provided: ItemStack, intendedMaterial: Material, deep: Boolean): ItemStack {
        val createdItem = this.createItem().second
        if (deep) {
            val item = createdItem.withMeta { meta ->
                provided.persistentDataContainer.copyTo(meta.persistentDataContainer, true)
            }
            if (item.type != intendedMaterial) {
                return item.withType(intendedMaterial)
            }
            return item
        } else {
            // only swap displayName and lore
            return provided.withMeta { meta ->
                val createdMeta = createdItem.itemMeta ?: return@withMeta
                meta.displayName(createdMeta.displayName())
                meta.lore(createdMeta.lore())
            }
        }
    }

    private fun ItemStack.getHandle(intendedMaterial: Material): CustomItem? {
        val toolType = Type.fromType(intendedMaterial) ?: return null
        val key = toolType.key.let { this.getPersistentKey(it, PersistentDataType.STRING) }
        return key?.let { Registry.CUSTOM_ITEMS.get(NamespacedIdentifier.of(it)) }
    }

    private fun ItemStack.setDecals(): ItemStack {
        return this.withMeta { meta ->
            val displayName = meta.displayName()?.asPlainText()
            meta.displayName(name(displayName ?: DEFAULT_NAME, true).asComponent())

            val lore = meta.lore() ?: run {
                meta.lore(listOf(CUSTOM_ENCHANT_COMPONENT))
                return@withMeta
            }

            val newLore = mutableListOf<Component>()
            var added = false
            for (i in lore.indices) {
                val line = lore[i]
                if (line.asPlainText().isBlank() && !added) {
                    newLore.add(i, CUSTOM_ENCHANT_COMPONENT)
                    added = true
                }
                newLore.add(line)
            }
            meta.lore(newLore)
        }
    }

    private fun ItemStack.cloneDurability(oldItem: ItemStack): ItemStack {
        return this.withMeta { meta ->
            val newMeta = meta as? Damageable ?: return@withMeta
            val oldMeta = oldItem.itemMeta as? Damageable ?: return@withMeta

            if (oldMeta.hasDamage()) {
                newMeta.damage = oldMeta.damage.coerceAtLeast(1)
            }
        }
    }

    private enum class Type(val toolType: ToolType, keyHandle: String, val material: Material) {
        PICKAXE(ToolType.PICKAXE, "horizons-toolkit-pickaxe", Material.NETHERITE_PICKAXE),
        SHOVEL(ToolType.SHOVEL, "horizons-toolkit-shovel", Material.NETHERITE_SHOVEL),
        AXE(ToolType.AXE, "horizons-toolkit-axe", Material.NETHERITE_AXE),
        HOE(ToolType.HOE, "horizons-toolkit-hoe", Material.NETHERITE_HOE),
        SWORD(ToolType.SWORD, "horizons-toolkit-sword", Material.NETHERITE_SWORD),
        SHEARS(ToolType.SHEARS, "horizons-toolkit-shears", Material.SHEARS);

        val key = keyHandle.namespacedKey()
        val itemStack = ItemStack.of(material)

        companion object {
            fun fromToolType(toolType: ToolType) = entries.firstOrNull { it.toolType == toolType }
            fun fromType(material: Material): Type? {
                val toolType = ToolType.getToolType(material) ?: return null
                return fromToolType(toolType)
            }

            fun getItemStacksExcluding(excluding: Material) = entries.map { it.itemStack }.filter { it.type != excluding }

        }
    }

}
