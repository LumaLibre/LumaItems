package dev.lumas.lumaitems.relics

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.astral.AstralSet
import dev.lumas.lumaitems.items.astral.AstralSetFunctions
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.namespacedKey
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

// todo: cleanup
object RelicCrafting {

    private val plugin: LumaItems = LumaItems.getInstance()
    val RELIC_KEY = "relic-item".namespacedKey()

    val relicShard: ItemStack = Util.createBasicItem(
        "&#E97979&lRelic &#F7FFC9Shard",
        mutableListOf(),
        Material.AMETHYST_SHARD,
        mutableListOf("relicshard"),
        true
    )

    val astralUpgradeCore: ItemStack = Util.createBasicItem(
        "&#AC87FB&lAstral &#F7FFC9Upgrade Core",
        mutableListOf(),
        Material.AMETHYST_CLUSTER,
        mutableListOf("astralupgradecore"),
        true
    )

    val lunarCore: ItemStack = Util.createBasicItem(
        "&#6255fb&lLunar &#F7FFC9Core",
        mutableListOf(),
        Material.PRISMARINE_SHARD,
        mutableListOf("lunarcore"),
        true
    )

    val astralCore: ItemStack = Util.createBasicItem(
        "&#AC87FB&lAstral &#F7FFC9Core",
        mutableListOf(),
        Material.PRISMARINE_SHARD,
        mutableListOf("astralcore"),
        true
    )

    val lunarOrb: ItemStack = Util.createBasicItem(
        "&#6255fb&lLunar &#F7FFC9Orb",
        mutableListOf("&7Right-click to redeem"),
        Material.ENDER_EYE,
        mutableListOf("lunarorb"),
        true
    )

    val astralOrb: ItemStack = Util.createBasicItem(
        "&#AC87FB&lAstral &#F7FFC9Orb",
        mutableListOf("&7Right-click to redeem"),
        Material.ENDER_EYE,
        mutableListOf("astralorb"),
        true
    )

    @JvmStatic
    fun registerRecipes() {
        registerIfMissing(NamespacedKey(plugin, "lunarorb"), lunarOrb, lunarCore)
        registerIfMissing(NamespacedKey(plugin, "astralorb"), astralOrb, astralCore)
        registerIfMissing(NamespacedKey(plugin, "astralupgradecore"), astralUpgradeCore, null)
    }

    private fun registerIfMissing(key: NamespacedKey, result: ItemStack, center: ItemStack?) {
        if (Bukkit.getRecipe(key) != null) return

        val recipe = ShapedRecipe(key, result)
        recipe.shape(
            "AAA",
            if (center == null) "A A" else "ABA",
            "AAA")
        recipe.setIngredient('A', relicShard)
        if (center != null) {
            recipe.setIngredient('B', center)
        }

        try {
            Bukkit.addRecipe(recipe, true)
        } catch (e: Exception) {
            LumaItems.LOGGER.error("Failed to register relic recipe $key", e)
        }
    }

    //TODO
    fun getItemsFromClass(className: String): List<ItemStack> {
        val clazz = try {
            Class.forName("dev.lumas.lumaitems.items.astral.sets.$className")
        } catch (e: ClassNotFoundException) {
            Class.forName("dev.lumas.lumaitems.items.astral.$className")
        }
        return if (AstralSet::class.java.isAssignableFrom(clazz)) {
            val astralSet = clazz.getDeclaredConstructor().newInstance() as AstralSet
            astralSet.setItems()
        } else if (AstralSetFunctions::class.java.isAssignableFrom(clazz)) {
            val astralSetFunctions = clazz.getDeclaredConstructor().newInstance() as AstralSetFunctions
            astralSetFunctions.setItems()
        } else {
            val item = clazz.getDeclaredConstructor().newInstance() as CustomItem
            listOf(item.createItem().second)
        }
    }

    fun <T : AstralSet> getItemsFromClass(clazz: Class<T>): List<ItemStack> {
        val astralSet = clazz.getDeclaredConstructor().newInstance() as AstralSet
        return astralSet.setItems()
    }

    fun hasFullSet(key: String, player: Player): Boolean {
        for (equipment in player.equipment.armorContents) {
            if (equipment == null || equipment.type == Material.AIR || !equipment.hasItemMeta()) return false
            if (!equipment.itemMeta.persistentDataContainer.has(NamespacedKey(plugin, key), PersistentDataType.SHORT)) return false
        }
        return true
    }
}