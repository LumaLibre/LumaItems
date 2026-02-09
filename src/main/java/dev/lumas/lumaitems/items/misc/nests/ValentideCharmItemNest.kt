package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.model.PersistentDataRecord
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.persistence.PersistentDataType

// Yes, I'll add functionality to this item someday. Maybe...
abstract class ValentideCharmItemNest(team: ValentideTeam) : CustomItemFunctions() {

    enum class ValentideTeam {
        HEARTBREAKERS,
        ROSETHORN,
        SWEETHEARTS
    }


    val nameSuffix = "<!b><#EEE1D5>Charm</#EEE1D5></!b>"
    val baseCharm = ItemFactory.builder()
        .lore(
            "A neat little charm you",
            "earned for participating",
            "in various minigames.",
            "",
            "You wonder what it does...",
            "Maybe you should keep it",
            "around for a while."
        )
        .tier(Tier.VALENTIDE_2025)
        .customEnchants("<gradient:#954381:#ED68B5>Charm</gradient>")
        .material(Material.RED_DYE)
        .persistentDataRecords(PersistentDataRecord.create("valentide-team", PersistentDataType.STRING, team.name))
        .persistentData("valentide-charm")
        .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 4)
}

class HeartBreakersValentideCharmItem : ValentideCharmItemNest(ValentideTeam.HEARTBREAKERS) {
    override fun createItem() = baseCharm
        .name("<b><gradient:#F33A71:#F8A5BE>Heart</gradient><gradient:#ABD9F8:#50B6FC>Breakers</gradient></b> $nameSuffix")
        .buildPair()
}

class RosethornValentideCharmItem : ValentideCharmItemNest(ValentideTeam.ROSETHORN) {
    override fun createItem() = baseCharm
        .name("<b><gradient:#FB124F:#F97BA2:#E34949>Rosethorn</gradient></b> $nameSuffix")
        .buildPair()
}

class SweetHeartsValentideCharmItem : ValentideCharmItemNest(ValentideTeam.SWEETHEARTS) {
    override fun createItem() = baseCharm
        .name("<b><gradient:#ffa0bc:#ffdaee:#ff9b9a:#ffe9e9>Sweethearts</gradient></b> $nameSuffix")
        .buildPair()
}