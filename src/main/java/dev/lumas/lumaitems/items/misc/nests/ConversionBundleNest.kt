package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.util.extensions.computeDyedBundleResult
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.persistence.PersistentDataType

class ReformationPouchBundleItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("reformation-pouch-bundle")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#BA8B41:#825927:#C15921:#F3B8B0:#E8E3E0>Reformation Pouch</gradient></b> <!b><#F7FFC9>Bundle</#F7FFC9></!b>")
            .material(Material.BUNDLE)
            .tier(Tier.VALENTIDE_2026)
            .persistentData(KEY)
            .lore(
                "<dark_gray>Right-click to open.",
                "",
                "A sealed pouch of",
                "arboreal patterns.",
                "",
                "Unseal it to reveal",
                "its chosen wood.",
                "",
                "Rough-cut or refined,",
                "wood is reworked to match."
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        item.amount -= 1

        val target = ReformationPouchItemNest.WoodSet.entries.random()
        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f)

        val pouch = when (target) {
            ReformationPouchItemNest.WoodSet.OAK -> OakReformationPouchItem()
            ReformationPouchItemNest.WoodSet.SPRUCE -> SpruceReformationPouchItem()
            ReformationPouchItemNest.WoodSet.BIRCH -> BirchReformationPouchItem()
            ReformationPouchItemNest.WoodSet.JUNGLE -> JungleReformationPouchItem()
            ReformationPouchItemNest.WoodSet.ACACIA -> AcaciaReformationPouchItem()
            ReformationPouchItemNest.WoodSet.DARK_OAK -> DarkOakReformationPouchItem()
            ReformationPouchItemNest.WoodSet.MANGROVE -> MangroveReformationPouchItem()
            ReformationPouchItemNest.WoodSet.CHERRY -> CherryReformationPouchItem()
            ReformationPouchItemNest.WoodSet.PALE_OAK -> PaleOakReformationPouchItem()
        }.createItem().second

        Util.giveItem(player, pouch)
    }
}

abstract class ReformationPouchItemNest(private val target: WoodSet) : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("reformation-pouch")
        private val WOOD_KEY = Util.namespacedKey("preformation-pouch-type")

        // TODO use enum
        // TODO bamboo
        private val ALLOWED_PREFIXES = setOf(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo"
        )
        private val SUFFIXES = listOf(
            "log", "wood",
            "planks", "stairs", "slab",
            "fence", "fence_gate",
            "door", "trapdoor",
            "pressure_plate", "button",
            "sign", "hanging_sign",
            "boat", "chest_boat"
        )

        private fun matchMat(key: String): Material? = Material.matchMaterial(key)

        private fun prefixOf(mat: Material, suffix: String): String? {
            val n = mat.name.lowercase()
            val s = "_$suffix"
            if (!n.endsWith(s)) return null
            return n.removeSuffix(s)
        }

        private fun strippedPrefixOf(mat: Material, tail: String): String? {
            val n = mat.name.lowercase()
            val prefix = "stripped_"
            val suffix = "_$tail"
            if (!n.startsWith(prefix) || !n.endsWith(suffix)) return null
            return n.removePrefix(prefix).removeSuffix(suffix)
        }

        private fun isAllowedPrefix(prefix: String?): Boolean {
            return prefix != null && prefix in ALLOWED_PREFIXES
        }

        fun convertMaterial(target: WoodSet, input: Material): Material? {
            val n = input.name.lowercase()

            if (n.endsWith("_leaves")) {
                val p = prefixOf(input, "leaves")
                if (!isAllowedPrefix(p)) return null
                return target.leaves
            }

            if (input == Material.MANGROVE_PROPAGULE) {
                return target.sapling
            }
            if (n.endsWith("_sapling")) {
                val p = prefixOf(input, "sapling")
                if (!isAllowedPrefix(p)) return null
                return target.sapling
            }

            if (n.startsWith("stripped_") && (n.endsWith("_log") || n.endsWith("_wood"))) {
                val tail = if (n.endsWith("_log")) "log" else "wood"
                val p = strippedPrefixOf(input, tail)
                if (!isAllowedPrefix(p)) return null
                return matchMat("stripped_${target.id}_$tail")
            }

            val suffix = SUFFIXES.firstOrNull { n.endsWith("_$it") } ?: return null
            val p = prefixOf(input, suffix) ?: return null
            if (!isAllowedPrefix(p)) return null
            return matchMat("${target.id}_$suffix")
        }

        private fun titleFor(target: WoodSet): String {
            val base = target.hexColor
            val dark = adjustHex(base, 0.85)
            val bright = adjustHex(base, 1.15)
            return "<b><gradient:#$dark:#$bright:#$dark:#$bright>Reformation Pouch</gradient></b>"
        }

        private fun clamp255(v: Int) = v.coerceIn(0, 255)

        private fun adjustHex(hex: String, factor: Double): String {
            val h = hex.removePrefix("#")
            require(h.length == 6) { "Expected RRGGBB hex, got: $hex" }

            val r = h.substring(0, 2).toInt(16)
            val g = h.substring(2, 4).toInt(16)
            val b = h.substring(4, 6).toInt(16)

            val nr = clamp255((r * factor).toInt())
            val ng = clamp255((g * factor).toInt())
            val nb = clamp255((b * factor).toInt())

            return "%02X%02X%02X".format(nr, ng, nb)
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name(titleFor(target))
            .customEnchants("<#${target.hexColor}>Conversion")
            .vanillaEnchants(Enchantment.UNBREAKING to 5)
            .hideEnchants(true)
            .material(target.bundle)
            .persistentData(KEY)
            .persistentDataRecords(
                PersistentDataRecord.create(
                    WOOD_KEY,
                    PersistentDataType.STRING,
                    target.id
                )
            )
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "Timber placed inside",
                "finds new lineage.",
                "",
                "Rough-cut or refined,",
                "all kindred wood."
            )
            .buildPair()
    }

    override fun onInventoryClick(player: Player, event: InventoryClickEvent) {
        val a = event.action
        if (a != InventoryAction.PLACE_ALL_INTO_BUNDLE
            && a != InventoryAction.PLACE_SOME_INTO_BUNDLE
            && a != InventoryAction.PICKUP_ALL_INTO_BUNDLE
            && a != InventoryAction.PICKUP_SOME_INTO_BUNDLE) return
        player.syncDelayed(1) {
            rewriteAllReformationPouchsInView(player)
            val cursorNow = player.itemOnCursor
            val rewrittenCursor = rewriteBundleIfNeeded(cursorNow)
            if (rewrittenCursor != null) {
                player.itemOnCursor = rewrittenCursor
            }
        }
    }

    private fun rewriteAllReformationPouchsInView(player: Player) {
        val inv = player.inventory
        for (i in 0 until inv.size) {
            val it = inv.getItem(i) ?: continue
            if (!Tag.ITEMS_BUNDLES.isTagged(it.type)) continue
            val rewritten = rewriteBundleIfNeeded(it) ?: continue
            inv.setItem(i, rewritten)
        }

        val top = player.openInventory.topInventory
        for (i in 0 until top.size) {
            val it = top.getItem(i) ?: continue
            val rewritten = rewriteBundleIfNeeded(it) ?: continue
            top.setItem(i, rewritten)
        }
    }

    private fun rewriteBundleIfNeeded(bundle: ItemStack): ItemStack? {
        if (bundle.type == Material.AIR) return null
        if (!bundle.isMatchingItem(KEY)) return null

        val woodId = Util.getPersistentKey(bundle, WOOD_KEY, PersistentDataType.STRING) ?: return null
        val targetSet = WoodSet.entries.firstOrNull { it.id == woodId } ?: return null

        val meta = bundle.itemMeta as? BundleMeta ?: return null
        val items = meta.items
        if (items.isEmpty()) return null

        var changed = false
        val convertedItems = items.map { stack ->
            val newType = convertMaterial(targetSet, stack.type)
            if (newType == null || newType == stack.type) {
                stack
            } else {
                changed = true
                ItemStack(newType, stack.amount).apply { itemMeta = stack.itemMeta }
            }
        }

        if (!changed) return null
        meta.setItems(convertedItems)
        val newBundle = bundle.clone()
        newBundle.itemMeta = meta
        return newBundle
    }

    enum class WoodSet(
        val id: String,
        val bundle: Material,
        val hexColor: String,
        val leaves: Material,
        val sapling: Material
    ) {
        OAK("oak", Material.BROWN_BUNDLE, "BA8B41", Material.OAK_LEAVES, Material.OAK_SAPLING),
        SPRUCE("spruce", Material.BUNDLE, "825927", Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING),
        BIRCH("birch", Material.LIGHT_GRAY_BUNDLE, "CDB768", Material.BIRCH_LEAVES, Material.BIRCH_SAPLING),
        JUNGLE("jungle", Material.BROWN_BUNDLE, "B67747", Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING),
        ACACIA("acacia", Material.ORANGE_BUNDLE, "C15921", Material.ACACIA_LEAVES, Material.ACACIA_SAPLING),
        DARK_OAK("dark_oak", Material.BLACK_BUNDLE, "502E0F", Material.DARK_OAK_LEAVES, Material.DARK_OAK_SAPLING),
        MANGROVE("mangrove", Material.RED_BUNDLE, "86352F", Material.MANGROVE_LEAVES, Material.MANGROVE_PROPAGULE),
        CHERRY("cherry", Material.PINK_BUNDLE, "F3B8B0", Material.CHERRY_LEAVES, Material.CHERRY_SAPLING),
        PALE_OAK("pale_oak", Material.WHITE_BUNDLE, "E8E3E0", Material.PALE_OAK_LEAVES, Material.PALE_OAK_SAPLING),
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        val result = computeDyedBundleResult(event.inventory.matrix, KEY.key) ?: return
        event.inventory.result = result
    }

    override fun onCraftItem(player: Player, event: CraftItemEvent) {
        val result = computeDyedBundleResult(event.inventory.matrix, KEY.key) ?: return
        event.currentItem = result
    }
}

class OakReformationPouchItem : ReformationPouchItemNest(WoodSet.OAK)
class SpruceReformationPouchItem : ReformationPouchItemNest(WoodSet.SPRUCE)
class BirchReformationPouchItem : ReformationPouchItemNest(WoodSet.BIRCH)
class JungleReformationPouchItem : ReformationPouchItemNest(WoodSet.JUNGLE)
class AcaciaReformationPouchItem : ReformationPouchItemNest(WoodSet.ACACIA)
class DarkOakReformationPouchItem : ReformationPouchItemNest(WoodSet.DARK_OAK)
class MangroveReformationPouchItem : ReformationPouchItemNest(WoodSet.MANGROVE)
class CherryReformationPouchItem : ReformationPouchItemNest(WoodSet.CHERRY)
class PaleOakReformationPouchItem : ReformationPouchItemNest(WoodSet.PALE_OAK)