package dev.lumas.lumaitems.items.weapons.trident

import com.oheers.fish.competition.Competition
import com.oheers.fish.config.MainConfig
import com.oheers.fish.fishing.Processor
import com.oheers.fish.messages.ConfigMessage
import dev.lumas.lumaitems.annotations.Requires
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import java.time.LocalDateTime
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

@Requires("EvenMoreFish")
class RosaliaGlaiveItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#C4C6FE:#FEE5DF:#FEC6C1:#FEDCA6:#FECA65>Rosalía Glaive</gradient></b>")
            .customEnchants("<#D6D7FC>Hydrox")
            .persistentData("rosalia-glaive")
            .tier(Tier.WONDERLAND_2026)
            .material(Material.TRIDENT)
            .vanillaEnchants(
                Enchantment.RIPTIDE to 5,
                Enchantment.UNBREAKING to 6,
                Enchantment.IMPALING to 10,
                Enchantment.MENDING to 1
            )
            .lore(
                "This trident's ability",
                "is exclusive to <#D6D7FC>aquatic</#D6D7FC>",
                "entities.",
                "",
                "In fish competitions, this",
                "trident may be used to",
                "<#D6D7FC>hunt</#D6D7FC> for and catch",
                "special types of fish.",
            )
            .buildPair()
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        val entity = event.entity
        if (!Tag.ENTITY_TYPES_AQUATIC.isTagged(entity.type) || entity.fromMobSpawner() || !EvenMoreFishDelegate.competitionOnlyCheck()) {
            return
        }

        val drops = event.drops

        val fishItemStacks = drops.filter { Tag.ITEMS_FISHES.isTagged(it.type) }

        fishItemStacks.forEach { item ->
            EvenMoreFishDelegate.getCaughtItem(player, player.location, player.inventory.itemInMainHand)?.let {
                drops.remove(item)
                drops.add(it)
            }
        }
    }

    private object EvenMoreFishDelegate : Processor<EntityDeathEvent>() {

        override fun process(event: EntityDeathEvent) {
            throw UnsupportedOperationException("Unsupported operation 'process'")
        }

        override fun isEnabled(): Boolean {
            throw UnsupportedOperationException("Unsupported operation 'isEnabled'")
        }

        override fun fireEvent(fish: com.oheers.fish.fishing.items.Fish, player: Player): Boolean {
            @Suppress("removal", "deprecation")
            return com.oheers.fish.api.EMFFishHuntEvent(fish, player, LocalDateTime.now()).callEvent()
        }

        override fun getCaughtMessage(): ConfigMessage {
            return ConfigMessage.FISH_HUNTED
        }

        override fun getLengthlessCaughtMessage(): ConfigMessage {
            return ConfigMessage.FISH_LENGTHLESS_HUNTED
        }

        public override fun competitionOnlyCheck(): Boolean {
            val active = Competition.getCurrentlyActive()
            return active?.competitionFile?.isAllowFishing ?: !MainConfig.getInstance().isFishCatchOnlyInCompetition
        }

        override fun shouldCatchBait(): Boolean {
            return false
        }

        override fun canUseFish(fish: com.oheers.fish.fishing.items.Fish): Boolean {
            return true
        }

    }
}