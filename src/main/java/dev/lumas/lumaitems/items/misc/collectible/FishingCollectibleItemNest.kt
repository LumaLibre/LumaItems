package dev.lumas.lumaitems.items.misc.collectible

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack


abstract class FishingCollectibleItemNest : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return create()
            .vanillaEnchants(Enchantment.LUCK_OF_THE_SEA to 1)
            .hideEnchants(true)
            .tier(Tier.COLLECTIBLE)
            .buildPair()
    }

    abstract fun create(): ItemFactory.Builder
}


class EchoPearlItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.GHAST_TEAR)
            .name("<gradient:#B7E4FB:#89AEFF><b>Echo Pearl")
            .lore(
                "<gray>A pearl left as the last",
                "<gray>echo was heard in the sea."
            )
            .persistentData("echo-pearl")
    }
}

class LeviathanScaleItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.PRISMARINE_SHARD)
            .name("<gradient:#7DFB9C:#428266><b>Leviathan Scale")
            .lore(
                "<gray>They say the scales are infused",
                "<gray>with the Leviathan's envy."
            )
            .persistentData("leviathan-scale")
    }
}

class WhisperingConchItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.NAUTILUS_SHELL)
            .name("<gradient:#CEB7FB:#FDAEA7><b>Whispering Conch")
            .lore(
                "<gray>If you listen close enough,",
                "<gray>you can still hear them."
            )
            .persistentData("whispering-conch")
    }
}

class FishersBootsItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.LEATHER_BOOTS)
            .name("<gradient:#78E7FB:#49A1FD><b>Fishers Boots")
            .lore(
                "<gray>Worn once by a promising fisher,",
                "<gray>never to be seen again."
            )
            .persistentData("fishers-boots")
    }
}

class FrostweaveFurItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.QUARTZ)
            .name("<gradient:#F0FBCD:#FDCA95><b>Frostweave Fur")
            .lore(
                "<gray>A furry sea creature?"
            )
            .persistentData("frostweave-fur")
    }
}

class GuardianGlaiveItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.TRIDENT)
            .name("<gradient:#6CEBA3:#9DE3C3><b>Guardian Glaive")
            .lore(
                "<gray>As the last guardian fell, his",
                "<gray>trident was lost at sea."
            )
            .persistentData("guardian-glaive")
    }
}

class PiratesBuriedMapItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.MAP)
            .name("<gradient:#FB2F66:#FF5A2E><b>Pirate's Buried Map")
            .lore(
                "<gray>Did somebody say ahoy?"
            )
            .persistentData("pirates-buried-map")
    }
}

class LostLanternItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.SOUL_LANTERN)
            .name("<gradient:#7B60FD:#5D8EFB><b>Lost Lantern")
            .lore(
                "<gray>A light source supported by the sirens."
            )
            .persistentData("lost-lantern")
    }
}

class CoralQuillItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.FEATHER)
            .name("<gradient:#F0C2B6:#B8CAE1><b>Coral Quill")
            .lore(
                "<gray>I wonder what words were written..."
            )
            .persistentData("coral-quill")
    }
}

class GhostfishEssenceItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.DRAGON_BREATH)
            .name("<gradient:#FB9191:#CC62B2><b>Ghostfish Essence")
            .lore(
                "<gray>A ghost was caught and trapped,",
                "<gray>but at what cost?"
            )
            .persistentData("ghostfish-essence")
    }
}

class AquaLotusItem : FishingCollectibleItemNest() {
    override fun create(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.WHITE_TULIP)
            .name("<gradient:#FBBCBC:#92FD93><b>Aqua Lotus")
            .lore(
                "<gray>A flower normally found on the sea bed."
            )
            .persistentData("aqua-lotus")
    }
}