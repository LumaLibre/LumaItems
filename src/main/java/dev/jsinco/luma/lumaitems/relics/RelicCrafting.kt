package dev.jsinco.luma.lumaitems.relics

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.astral.AstralSet
import dev.jsinco.luma.lumaitems.items.astral.AstralSetFunctions
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

object RelicCrafting {

    private val plugin: LumaItems = LumaItems.getInstance()

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
        val lunarKey = NamespacedKey(plugin, "lunarorb")
        val astralKey = NamespacedKey(plugin, "astralorb")
        val upgradeCoreKey = NamespacedKey(plugin, "astralupgradecore")

        if (Bukkit.getRecipe(lunarKey) != null) Bukkit.removeRecipe(lunarKey)
        if (Bukkit.getRecipe(astralKey) != null) Bukkit.removeRecipe(astralKey)
        if (Bukkit.getRecipe(upgradeCoreKey) != null) Bukkit.removeRecipe(upgradeCoreKey)


        val lunarRecipe = ShapedRecipe(lunarKey, lunarOrb)
        lunarRecipe.shape(
            "AAA",
            "ABA",
            "AAA")
        lunarRecipe.setIngredient('A', relicShard)
        lunarRecipe.setIngredient('B', lunarCore)
        Bukkit.addRecipe(lunarRecipe)

        val astralRecipe = ShapedRecipe(astralKey, astralOrb)
        astralRecipe.shape(
            "AAA",
            "ABA",
            "AAA")
        astralRecipe.setIngredient('A', relicShard)
        astralRecipe.setIngredient('B', astralCore)
        Bukkit.addRecipe(astralRecipe)

        val upgradeCoreRecipe = ShapedRecipe(upgradeCoreKey, astralUpgradeCore)
        upgradeCoreRecipe.shape(
            "AAA",
            "ABA",
            "AAA")
        upgradeCoreRecipe.setIngredient('A', relicShard)
        Bukkit.addRecipe(upgradeCoreRecipe)
    }

    fun getItemsFromClass(className: String): List<ItemStack> {
        val clazz = try {
            Class.forName("dev.jsinco.luma.lumaitems.items.astral.sets.$className")
        } catch (e: ClassNotFoundException) {
            Class.forName("dev.jsinco.luma.lumaitems.items.astral.$className")
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

    fun hasFullSet(key: String, player: Player): Boolean {
        for (equipment in player.equipment.armorContents) {
            if (equipment == null || equipment.type == Material.AIR || !equipment.hasItemMeta()) return false
            if (!equipment.itemMeta.persistentDataContainer.has(NamespacedKey(plugin, key), PersistentDataType.SHORT)) return false
        }
        return true
    }
}